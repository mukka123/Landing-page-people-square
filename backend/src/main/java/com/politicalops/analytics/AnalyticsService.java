package com.politicalops.analytics;

import com.politicalops.analytics.dto.TrendPoint;
import com.politicalops.common.TenantContext;
import com.politicalops.elections.Election;
import com.politicalops.elections.ElectionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Read-only aggregate query layer. This — not raw booth_result scans, and not a free-form
 * SQL string from the AI layer — is what both the dashboard and the "natural language
 * analytics" AI capability call: the HLD requires AI questions to go through "governed SQL
 * or metric queries", and this service IS that governed layer.
 */
@Service
public class AnalyticsService {

    private final AnalyticsSnapshotRepository snapshotRepository;
    private final ElectionRepository electionRepository;

    public AnalyticsService(AnalyticsSnapshotRepository snapshotRepository, ElectionRepository electionRepository) {
        this.snapshotRepository = snapshotRepository;
        this.electionRepository = electionRepository;
    }

    /**
     * Historical trend across elections for geography units the caller is authorized for.
     * geographicUnitIds is intersected with TenantContext.authorizedGeographicUnitIds()
     * server-side; a caller cannot widen their own scope by passing extra ids.
     */
    public List<TrendPoint> trends(List<UUID> requestedGeographicUnitIds, Metric metric) {
        TenantContext ctx = TenantContext.current();
        List<UUID> authorizedIds = requestedGeographicUnitIds.stream()
                .filter(ctx.authorizedGeographicUnitIds()::contains)
                .collect(Collectors.toList());

        if (authorizedIds.isEmpty()) {
            return List.of(); // nothing authorized — return empty, not an error, to avoid leaking existence
        }

        List<AnalyticsSnapshot> snapshots = snapshotRepository
                .findByTenantIdAndGeographicUnitIdInAndMetric(ctx.tenantId(), authorizedIds, metric);

        Map<UUID, Election> electionsById = electionRepository.findAllById(
                        snapshots.stream().map(AnalyticsSnapshot::getElectionId).distinct().toList())
                .stream().collect(Collectors.toMap(Election::getId, e -> e));

        return snapshots.stream()
                .map(s -> new TrendPoint(
                        s.getElectionId(),
                        electionsById.containsKey(s.getElectionId()) ? electionsById.get(s.getElectionId()).getYear() : 0,
                        s.getGeographicUnitId(), s.getPartyId(), s.getValue(), s.getFormulaVersion()))
                .sorted((a, b) -> Integer.compare(a.electionYear(), b.electionYear()))
                .collect(Collectors.toList());
    }
}
