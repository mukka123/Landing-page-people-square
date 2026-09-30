package com.politicalops.elections;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "turnout")
public class Turnout {
    @Id @GeneratedValue
    private UUID id;

    @Column(name = "polling_station_id", nullable = false, unique = true)
    private UUID pollingStationId;

    private int male, female, other;

    @Column(name = "total_polled")
    private int totalPolled;

    private int rejected;
    private int nota;
    private int tendered;

    @Column(nullable = false)
    private boolean reconciled = false;

    @Column(name = "reconciliation_note")
    private String reconciliationNote;

    protected Turnout() {}

    public Turnout(UUID pollingStationId, int male, int female, int other,
                    int totalPolled, int rejected, int nota, int tendered) {
        this.pollingStationId = pollingStationId;
        this.male = male; this.female = female; this.other = other;
        this.totalPolled = totalPolled; this.rejected = rejected;
        this.nota = nota; this.tendered = tendered;
    }

    public UUID getPollingStationId() { return pollingStationId; }
    public int getTotalPolled() { return totalPolled; }
    public boolean isReconciled() { return reconciled; }

    public void markReconciled(boolean ok, String note) {
        this.reconciled = ok;
        this.reconciliationNote = note;
    }
}
