package com.politicalops.geography;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;
import java.time.Instant;
import java.util.UUID;

/**
 * A polling station AS IT EXISTED IN ONE ELECTION. partNumber is only unique within that
 * election — it is never a stable cross-year identifier. Cross-year comparison always goes
 * through PollingStationCrosswalk to a stable GeographicUnit of type CLUSTER.
 */
@Entity
@Table(name = "polling_station")
public class PollingStation {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "election_id", nullable = false)
    private UUID electionId;

    @Column(name = "part_number", nullable = false)
    private int partNumber;

    @Column(nullable = false)
    private String name;

    private String address;

    @Column(columnDefinition = "geometry(Point,4326)")
    private Point location;

    @Column(name = "elector_count")
    private Integer electorCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected PollingStation() {}

    public PollingStation(UUID electionId, int partNumber, String name) {
        this.electionId = electionId;
        this.partNumber = partNumber;
        this.name = name;
    }

    public UUID getId() { return id; }
    public UUID getElectionId() { return electionId; }
    public int getPartNumber() { return partNumber; }
    public String getName() { return name; }
}
