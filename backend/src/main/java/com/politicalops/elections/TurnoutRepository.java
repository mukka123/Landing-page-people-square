package com.politicalops.elections;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface TurnoutRepository extends JpaRepository<Turnout, UUID> {
    Optional<Turnout> findByPollingStationId(UUID pollingStationId);
}
