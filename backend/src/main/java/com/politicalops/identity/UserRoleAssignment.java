package com.politicalops.identity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A single role grant for a user, scoped to one geographic unit (or tenant-wide for
 * PLATFORM_ADMIN, where geographicUnitId is null). This is the row every authorization
 * check ultimately joins against.
 */
@Entity
@Table(name = "user_role_assignment")
public class UserRoleAssignment {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "role_code", nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleCode roleCode;

    @Column(name = "geographic_unit_id")
    private UUID geographicUnitId;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom = LocalDate.now();

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected UserRoleAssignment() {}

    public UserRoleAssignment(UUID userId, RoleCode roleCode, UUID geographicUnitId) {
        this.userId = userId;
        this.roleCode = roleCode;
        this.geographicUnitId = geographicUnitId;
    }

    public UUID getUserId() { return userId; }
    public RoleCode getRoleCode() { return roleCode; }
    public UUID getGeographicUnitId() { return geographicUnitId; }
    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(effectiveFrom) && (effectiveTo == null || today.isBefore(effectiveTo));
    }
}
