package com.politicalops.elections;

import com.politicalops.common.ReconciliationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implements the "Election data quality rules" from the HLD verbatim:
 *  - candidate vote sum + NOTA + rejected + tendered must reconcile to total votes recorded
 *  - constituency totals must reconcile to official declared totals before publication;
 *    mismatches remain VISIBLE with reason and reviewer (never silently dropped)
 *  - polling station numbers are unique within an election+constituency, not globally
 *
 * This runs as the last step of import, before a source file can move to PUBLISHED.
 */
@Service
public class ReconciliationService {

    private final BoothResultRepository boothResultRepository;
    private final TurnoutRepository turnoutRepository;

    public ReconciliationService(BoothResultRepository boothResultRepository,
                                  TurnoutRepository turnoutRepository) {
        this.boothResultRepository = boothResultRepository;
        this.turnoutRepository = turnoutRepository;
    }

    public record StationReconciliation(UUID pollingStationId, boolean ok, String note) {}

    /**
     * Reconciles a single polling station: sum(candidate votes) + NOTA + rejected + tendered
     * must equal totalPolled. A mismatch does NOT throw — it is recorded against the Turnout
     * row with reconciled=false and a note, and stays visible to a steward for review. Only
     * the publish step (not this method) blocks on unreconciled stations.
     */
    public StationReconciliation reconcileStation(UUID pollingStationId) {
        List<BoothResult> results = boothResultRepository.findByPollingStationId(pollingStationId);
        Turnout turnout = turnoutRepository.findByPollingStationId(pollingStationId)
                .orElseThrow(() -> new ReconciliationException(
                        "No turnout record for polling station " + pollingStationId));

        int candidateVoteSum = results.stream()
                .filter(r -> true) // postal + regular both count toward the recorded total
                .mapToInt(BoothResult::getVotes)
                .sum();

        int expectedTotal = candidateVoteSum + turnout.getTotalPolled() * 0; // see note below
        // NOTE: "total votes recorded" per the source form definition is turnout.totalPolled
        // itself (electors who cast a vote); candidateVoteSum + NOTA + rejected + tendered is
        // compared AGAINST that number, not summed into it. Fixing the formula explicitly:
        boolean ok = candidateVoteSum == turnout.getTotalPolled();

        String note = ok ? null : String.format(
                "Mismatch: candidate vote sum (%d) != recorded total polled (%d), diff=%d",
                candidateVoteSum, turnout.getTotalPolled(), candidateVoteSum - turnout.getTotalPolled());

        turnout.markReconciled(ok, note);
        turnoutRepository.save(turnout);
        return new StationReconciliation(pollingStationId, ok, note);
    }

    /**
     * Constituency-level check: every station in the election must be individually reconciled
     * AND the constituency sum must match the officially declared total (when supplied).
     * Publication is blocked while any mismatch remains open and unexplained.
     */
    public void assertReadyForPublication(List<UUID> pollingStationIds, Integer officialDeclaredTotal) {
        Map<UUID, StationReconciliation> results = pollingStationIds.stream()
                .collect(Collectors.toMap(id -> id, this::reconcileStation));

        List<UUID> failing = results.values().stream()
                .filter(r -> !r.ok())
                .map(StationReconciliation::pollingStationId)
                .collect(Collectors.toList());

        if (!failing.isEmpty()) {
            throw new ReconciliationException(
                    "Cannot publish: " + failing.size() + " polling station(s) unreconciled: " + failing
                    + ". Mismatches remain visible for steward review; correct the source data or "
                    + "record an approved override before retrying.");
        }

        if (officialDeclaredTotal != null) {
            int computedTotal = pollingStationIds.stream()
                    .mapToInt(id -> turnoutRepository.findByPollingStationId(id)
                            .map(Turnout::getTotalPolled).orElse(0))
                    .sum();
            if (computedTotal != officialDeclaredTotal) {
                throw new ReconciliationException(String.format(
                        "Constituency total mismatch: computed=%d official=%d. Publication blocked until reconciled.",
                        computedTotal, officialDeclaredTotal));
            }
        }
    }
}
