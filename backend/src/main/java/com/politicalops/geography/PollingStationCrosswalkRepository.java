package com.politicalops.geography;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PollingStationCrosswalkRepository extends JpaRepository<PollingStationCrosswalk, UUID> {
    Optional<PollingStationCrosswalk> findByFromPollingStationId(UUID fromPollingStationId);
    List<PollingStationCrosswalk> findByApprovalStatus(CrosswalkApprovalStatus status);
}
