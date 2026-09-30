package com.politicalops.geography;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PollingStationRepository extends JpaRepository<PollingStation, UUID> {
    List<PollingStation> findByElectionId(UUID electionId);
    Optional<PollingStation> findByElectionIdAndPartNumber(UUID electionId, int partNumber);
}
