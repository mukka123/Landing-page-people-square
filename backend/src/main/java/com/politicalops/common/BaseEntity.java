package com.politicalops.common;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Base for every persistent aggregate root. Every table in the curated zone carries a
 * tenant boundary and a created_at, matching the "Data zones" / "Core entity model"
 * sections of the HLD — this keeps that convention from being repeated (and forgotten)
 * on each entity.
 */
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public Instant getCreatedAt() { return createdAt; }
}
