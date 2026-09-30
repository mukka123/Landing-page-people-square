package com.politicalops.elections;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "candidate")
public class Candidate {
    @Id @GeneratedValue
    private UUID id;

    @Column(name = "election_id", nullable = false)
    private UUID electionId;

    @Column(name = "party_id")
    private UUID partyId;

    @Column(nullable = false)
    private String name;

    @Column(name = "source_ref")
    private String sourceRef;

    protected Candidate() {}
    public Candidate(UUID electionId, UUID partyId, String name) {
        this.electionId = electionId; this.partyId = partyId; this.name = name;
    }

    public UUID getId() { return id; }
    public UUID getElectionId() { return electionId; }
    public UUID getPartyId() { return partyId; }
    public String getName() { return name; }
}
