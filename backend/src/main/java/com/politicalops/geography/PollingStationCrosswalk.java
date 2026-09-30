package com.politicalops.geography;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "polling_station_crosswalk")
public class PollingStationCrosswalk {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "from_polling_station_id", nullable = false)
    private UUID fromPollingStationId;

    @Column(name = "to_geographic_unit_id", nullable = false)
    private UUID toGeographicUnitId;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_method", nullable = false)
    private CrosswalkMatchMethod matchMethod;

    @Column(nullable = false)
    private java.math.BigDecimal confidence;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    private CrosswalkApprovalStatus approvalStatus = CrosswalkApprovalStatus.PENDING;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected PollingStationCrosswalk() {}

    public PollingStationCrosswalk(UUID fromPollingStationId, UUID toGeographicUnitId,
                                    CrosswalkMatchMethod matchMethod, java.math.BigDecimal confidence) {
        this.fromPollingStationId = fromPollingStationId;
        this.toGeographicUnitId = toGeographicUnitId;
        this.matchMethod = matchMethod;
        this.confidence = confidence;
    }

    public UUID getId() { return id; }
    public UUID getFromPollingStationId() { return fromPollingStationId; }
    public UUID getToGeographicUnitId() { return toGeographicUnitId; }
    public java.math.BigDecimal getConfidence() { return confidence; }
    public CrosswalkApprovalStatus getApprovalStatus() { return approvalStatus; }

    public void approve(UUID approverId) {
        this.approvalStatus = CrosswalkApprovalStatus.APPROVED;
        this.approvedBy = approverId;
        this.approvedAt = Instant.now();
    }

    public void reject(UUID approverId) {
        this.approvalStatus = CrosswalkApprovalStatus.REJECTED;
        this.approvedBy = approverId;
        this.approvedAt = Instant.now();
    }
}
