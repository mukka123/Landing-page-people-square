package com.politicalops.analytics;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * A precomputed metric value (HLD nonfunctional requirement: "precompute election aggregates
 * ... and refresh on publication events"). Every row carries formulaVersion + inputSnapshotRef
 * so a value can always be explained and reproduced — this is what the AI trend-explanation
 * capability cites, and what the "evidence first" architecture principle requires.
 */
@Entity
@Table(name = "analytics_snapshot")
public class AnalyticsSnapshot {

    @Id @GeneratedValue
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "geographic_unit_id", nullable = false)
    private UUID geographicUnitId;

    @Column(name = "election_id", nullable = false)
    private UUID electionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Metric metric;

    @Column(name = "party_id")
    private UUID partyId;

    @Column(nullable = false)
    private BigDecimal value;

    @Column(name = "formula_version", nullable = false)
    private String formulaVersion;

    @Column(name = "input_snapshot_ref", nullable = false)
    private String inputSnapshotRef;

    @Column(name = "refreshed_at", nullable = false)
    private Instant refreshedAt = Instant.now();

    protected AnalyticsSnapshot() {}

    public AnalyticsSnapshot(UUID tenantId, UUID geographicUnitId, UUID electionId, Metric metric,
                              UUID partyId, BigDecimal value, String formulaVersion, String inputSnapshotRef) {
        this.tenantId = tenantId;
        this.geographicUnitId = geographicUnitId;
        this.electionId = electionId;
        this.metric = metric;
        this.partyId = partyId;
        this.value = value;
        this.formulaVersion = formulaVersion;
        this.inputSnapshotRef = inputSnapshotRef;
    }

    public UUID getGeographicUnitId() { return geographicUnitId; }
    public UUID getElectionId() { return electionId; }
    public Metric getMetric() { return metric; }
    public UUID getPartyId() { return partyId; }
    public BigDecimal getValue() { return value; }
    public String getFormulaVersion() { return formulaVersion; }
}
