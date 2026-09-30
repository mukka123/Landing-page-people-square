package com.politicalops.elections;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "booth_result")
public class BoothResult {
    @Id @GeneratedValue
    private UUID id;

    @Column(name = "polling_station_id", nullable = false)
    private UUID pollingStationId;

    @Column(name = "candidate_id", nullable = false)
    private UUID candidateId;

    @Column(nullable = false)
    private int votes;

    @Column(name = "postal_flag", nullable = false)
    private boolean postalFlag = false;

    @Column(name = "source_file_id", nullable = false)
    private UUID sourceFileId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected BoothResult() {}

    public BoothResult(UUID pollingStationId, UUID candidateId, int votes, boolean postalFlag, UUID sourceFileId) {
        if (votes < 0) throw new IllegalArgumentException("votes cannot be negative");
        this.pollingStationId = pollingStationId;
        this.candidateId = candidateId;
        this.votes = votes;
        this.postalFlag = postalFlag;
        this.sourceFileId = sourceFileId;
    }

    public UUID getPollingStationId() { return pollingStationId; }
    public UUID getCandidateId() { return candidateId; }
    public int getVotes() { return votes; }
}
