package com.politicalops.elections;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Lineage record for every imported result file — never overwritten, only superseded. */
@Entity
@Table(name = "election_source_file")
public class ElectionSourceFile {

    public enum Status { RECEIVED, PARSING, PARSED, VALIDATION_FAILED, RECONCILED, PUBLISHED }

    @Id @GeneratedValue
    private UUID id;

    @Column(name = "election_id", nullable = false)
    private UUID electionId;

    @Column(name = "storage_key", nullable = false)
    private String storageKey;

    @Column(nullable = false)
    private String checksum;

    @Column(name = "uploaded_by", nullable = false)
    private UUID uploadedBy;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt = Instant.now();

    @Column(name = "parser_version", nullable = false)
    private String parserVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.RECEIVED;

    protected ElectionSourceFile() {}

    public ElectionSourceFile(UUID electionId, String storageKey, String checksum,
                               UUID uploadedBy, String parserVersion) {
        this.electionId = electionId;
        this.storageKey = storageKey;
        this.checksum = checksum;
        this.uploadedBy = uploadedBy;
        this.parserVersion = parserVersion;
    }

    public UUID getId() { return id; }
    public UUID getElectionId() { return electionId; }
    public Status getStatus() { return status; }
    public void markStatus(Status status) { this.status = status; }
}
