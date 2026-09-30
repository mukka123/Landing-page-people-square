package com.politicalops.elections;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface BoothResultRepository extends JpaRepository<BoothResult, UUID> {
    List<BoothResult> findByPollingStationId(UUID pollingStationId);
    List<BoothResult> findByCandidateIdIn(List<UUID> candidateIds);
}
