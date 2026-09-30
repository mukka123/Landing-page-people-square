package com.politicalops.analytics;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AnalyticsSnapshotRepository extends JpaRepository<AnalyticsSnapshot, UUID> {

    List<AnalyticsSnapshot> findByTenantIdAndGeographicUnitIdInAndMetric(
            UUID tenantId, List<UUID> geographicUnitIds, Metric metric);

    List<AnalyticsSnapshot> findByTenantIdAndGeographicUnitIdInAndElectionIdInAndMetric(
            UUID tenantId, List<UUID> geographicUnitIds, List<UUID> electionIds, Metric metric);
}
