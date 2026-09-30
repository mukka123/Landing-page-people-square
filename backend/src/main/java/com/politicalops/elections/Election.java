package com.politicalops.elections;

import com.politicalops.common.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "election")
public class Election extends BaseEntity {

    public enum Type { ASSEMBLY, PARLIAMENT, LOCAL_BODY }
    public enum Status { DRAFT, STAGED, PUBLISHED, ARCHIVED }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Column(nullable = false)
    private int year;

    @Column(name = "election_date")
    private LocalDate electionDate;

    @Column(name = "constituency_id", nullable = false)
    private UUID constituencyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.DRAFT;

    protected Election() {}

    public Election(Type type, int year, UUID constituencyId) {
        this.type = type;
        this.year = year;
        this.constituencyId = constituencyId;
    }

    public int getYear() { return year; }
    public UUID getConstituencyId() { return constituencyId; }
    public Status getStatus() { return status; }
    public void publish() { this.status = Status.PUBLISHED; }
}
