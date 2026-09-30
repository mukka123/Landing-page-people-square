package com.politicalops.geography;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Proposes a polling-station -> stable-cluster mapping (HLD capability: "Booth identity
 * resolution"). This is a deterministic name/location matcher, NOT the AI orchestrator —
 * per the HLD, AI may *suggest* mappings via the same proposal shape, but every proposal
 * (deterministic or AI-assisted) lands in PENDING and requires a stewards approval before
 * any analytics query is allowed to use it. Nothing here auto-publishes.
 */
@Service
public class CrosswalkProposalService {

    private final PollingStationCrosswalkRepository crosswalkRepository;

    public CrosswalkProposalService(PollingStationCrosswalkRepository crosswalkRepository) {
        this.crosswalkRepository = crosswalkRepository;
    }

    /**
     * Confidence bands (tune from real matching data before Phase One sign-off):
     *   >= 0.90 exact name + within 150m of prior known location -> HYBRID
     *   >= 0.75 normalized name match only                        -> NAME_MATCH
     *   >= 0.60 location proximity only                            -> LOCATION_MATCH
     *   below threshold -> no proposal is created; steward must map manually.
     */
    private static final BigDecimal AUTO_PROPOSAL_FLOOR = new BigDecimal("0.60");

    public Optional<PollingStationCrosswalk> proposeMapping(
            PollingStation station, List<CandidateClusterMatch> candidates) {

        CandidateClusterMatch best = candidates.stream()
                .max((a, b) -> a.confidence().compareTo(b.confidence()))
                .orElse(null);

        if (best == null || best.confidence().compareTo(AUTO_PROPOSAL_FLOOR) < 0) {
            return Optional.empty(); // no confident match — leave for manual mapping
        }

        PollingStationCrosswalk proposal = new PollingStationCrosswalk(
                station.getId(), best.clusterGeographicUnitId(), best.method(), best.confidence());
        return Optional.of(crosswalkRepository.save(proposal));
    }

    public PollingStationCrosswalk approve(java.util.UUID crosswalkId, java.util.UUID approverId) {
        PollingStationCrosswalk crosswalk = crosswalkRepository.findById(crosswalkId)
                .orElseThrow(() -> new com.politicalops.common.NotFoundException("Crosswalk proposal not found"));
        crosswalk.approve(approverId);
        return crosswalkRepository.save(crosswalk);
    }

    public record CandidateClusterMatch(java.util.UUID clusterGeographicUnitId,
                                         CrosswalkMatchMethod method,
                                         BigDecimal confidence) {}
}
