package com.politicalops.geography;

import com.politicalops.common.BaseEntity;
import jakarta.persistence.*;
import org.locationtech.jts.geom.MultiPolygon;
import java.time.LocalDate;

/**
 * A versioned node in the constituency > mandal/municipality > ward/village > colony >
 * cluster hierarchy. "Versioned" matters: boundaries and booth-to-area mappings change
 * between elections, so every unit carries effective dates rather than being edited in place.
 */
@Entity
@Table(name = "geographic_unit")
public class GeographicUnit extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GeographicUnitType type;

    @Column(nullable = false)
    private String name;

    @Column(name = "parent_id")
    private java.util.UUID parentId;

    @Column(columnDefinition = "geometry(MultiPolygon,4326)")
    private MultiPolygon geometry;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    protected GeographicUnit() {}

    public GeographicUnit(GeographicUnitType type, String name, java.util.UUID parentId, LocalDate effectiveFrom) {
        this.type = type;
        this.name = name;
        this.parentId = parentId;
        this.effectiveFrom = effectiveFrom;
    }

    public GeographicUnitType getType() { return type; }
    public String getName() { return name; }
    public java.util.UUID getParentId() { return parentId; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public LocalDate getEffectiveTo() { return effectiveTo; }
    public boolean isActiveOn(LocalDate date) {
        return !date.isBefore(effectiveFrom) && (effectiveTo == null || date.isBefore(effectiveTo));
    }
}
