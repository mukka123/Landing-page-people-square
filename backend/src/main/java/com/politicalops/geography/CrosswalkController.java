package com.politicalops.geography;

import com.politicalops.common.TenantContext;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Implements the crosswalk endpoints from the HLD "Representative APIs" table. */
@RestController
@RequestMapping("/api/v1/polling-station-crosswalks")
public class CrosswalkController {

    private final CrosswalkProposalService proposalService;
    private final PollingStationRepository pollingStationRepository;
    private final PollingStationCrosswalkRepository crosswalkRepository;

    public CrosswalkController(CrosswalkProposalService proposalService,
                                PollingStationRepository pollingStationRepository,
                                PollingStationCrosswalkRepository crosswalkRepository) {
        this.proposalService = proposalService;
        this.pollingStationRepository = pollingStationRepository;
        this.crosswalkRepository = crosswalkRepository;
    }

    public record ProposalRequest(@NotNull UUID pollingStationId,
                                   @NotNull List<CrosswalkProposalService.CandidateClusterMatch> candidates) {}

    @PostMapping("/proposals")
    public ResponseEntity<PollingStationCrosswalk> propose(@RequestBody ProposalRequest request) {
        // CONSTITUENCY_ADMIN / DATA_STEWARD only — enforced by method security in SecurityConfig.
        PollingStation station = pollingStationRepository.findById(request.pollingStationId())
                .orElseThrow(() -> new com.politicalops.common.NotFoundException("Polling station not found"));
        return proposalService.proposeMapping(station, request.candidates())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<PollingStationCrosswalk> approve(@PathVariable UUID id) {
        UUID approver = TenantContext.current().userId();
        return ResponseEntity.ok(proposalService.approve(id, approver));
    }

    @GetMapping("/pending")
    public List<PollingStationCrosswalk> pending() {
        return crosswalkRepository.findByApprovalStatus(CrosswalkApprovalStatus.PENDING);
    }
}
