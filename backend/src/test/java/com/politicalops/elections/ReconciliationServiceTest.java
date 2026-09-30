package com.politicalops.elections;

import com.politicalops.common.ReconciliationException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the election data quality rules (HLD section 6). These are the rules the
 * whole "trusted booth result dataset" promise rests on, so they are tested directly rather
 * than only through an end-to-end import — a mismatch here must never silently pass.
 */
class ReconciliationServiceTest {

    private final BoothResultRepository boothResultRepository = Mockito.mock(BoothResultRepository.class);
    private final TurnoutRepository turnoutRepository = Mockito.mock(TurnoutRepository.class);
    private final ReconciliationService service =
            new ReconciliationService(boothResultRepository, turnoutRepository);

    @Test
    void reconciles_when_candidate_vote_sum_matches_total_polled() {
        UUID stationId = UUID.randomUUID();
        UUID sourceId = UUID.randomUUID();

        when(boothResultRepository.findByPollingStationId(stationId)).thenReturn(List.of(
                new BoothResult(stationId, UUID.randomUUID(), 400, false, sourceId),
                new BoothResult(stationId, UUID.randomUUID(), 350, false, sourceId),
                new BoothResult(stationId, UUID.randomUUID(), 50, false, sourceId) // NOTA-as-candidate-row
        ));
        Turnout turnout = new Turnout(stationId, 400, 380, 0, 800, 0, 0, 0);
        when(turnoutRepository.findByPollingStationId(stationId)).thenReturn(Optional.of(turnout));

        var result = service.reconcileStation(stationId);

        assertTrue(result.ok(), "800 candidate votes should match 800 total polled");
        assertNull(result.note());
    }

    @Test
    void flags_mismatch_without_throwing_and_records_the_reason() {
        UUID stationId = UUID.randomUUID();
        UUID sourceId = UUID.randomUUID();

        when(boothResultRepository.findByPollingStationId(stationId)).thenReturn(List.of(
                new BoothResult(stationId, UUID.randomUUID(), 400, false, sourceId),
                new BoothResult(stationId, UUID.randomUUID(), 350, false, sourceId)
        ));
        Turnout turnout = new Turnout(stationId, 400, 380, 0, 800, 0, 0, 0); // 750 vs 800
        when(turnoutRepository.findByPollingStationId(stationId)).thenReturn(Optional.of(turnout));

        var result = service.reconcileStation(stationId);

        assertFalse(result.ok());
        assertNotNull(result.note());
        assertTrue(result.note().contains("diff=-50"));
    }

    @Test
    void publish_is_blocked_while_any_station_is_unreconciled() {
        UUID goodStation = UUID.randomUUID();
        UUID badStation = UUID.randomUUID();
        UUID sourceId = UUID.randomUUID();

        when(boothResultRepository.findByPollingStationId(goodStation))
                .thenReturn(List.of(new BoothResult(goodStation, UUID.randomUUID(), 500, false, sourceId)));
        when(turnoutRepository.findByPollingStationId(goodStation))
                .thenReturn(Optional.of(new Turnout(goodStation, 250, 250, 0, 500, 0, 0, 0)));

        when(boothResultRepository.findByPollingStationId(badStation))
                .thenReturn(List.of(new BoothResult(badStation, UUID.randomUUID(), 300, false, sourceId)));
        when(turnoutRepository.findByPollingStationId(badStation))
                .thenReturn(Optional.of(new Turnout(badStation, 200, 200, 0, 400, 0, 0, 0))); // mismatch

        ReconciliationException ex = assertThrows(ReconciliationException.class, () ->
                service.assertReadyForPublication(List.of(goodStation, badStation), null));

        assertTrue(ex.getMessage().contains(badStation.toString()));
        assertFalse(ex.getMessage().contains(goodStation.toString()),
                "only the failing station should be listed");
    }

    @Test
    void publish_is_blocked_when_constituency_total_does_not_match_official_declared_total() {
        UUID stationId = UUID.randomUUID();
        UUID sourceId = UUID.randomUUID();

        when(boothResultRepository.findByPollingStationId(stationId))
                .thenReturn(List.of(new BoothResult(stationId, UUID.randomUUID(), 500, false, sourceId)));
        when(turnoutRepository.findByPollingStationId(stationId))
                .thenReturn(Optional.of(new Turnout(stationId, 250, 250, 0, 500, 0, 0, 0)));

        ReconciliationException ex = assertThrows(ReconciliationException.class, () ->
                service.assertReadyForPublication(List.of(stationId), 550));

        assertTrue(ex.getMessage().contains("Constituency total mismatch"));
    }
}
