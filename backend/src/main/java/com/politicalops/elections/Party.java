package com.politicalops.elections;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "party")
public class Party {
    @Id @GeneratedValue
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String name;

    private String symbol;

    protected Party() {}
    public Party(UUID tenantId, String name, String symbol) {
        this.tenantId = tenantId; this.name = name; this.symbol = symbol;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
}
