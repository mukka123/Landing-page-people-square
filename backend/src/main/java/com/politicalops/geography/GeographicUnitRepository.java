package com.politicalops.geography;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface GeographicUnitRepository extends JpaRepository<GeographicUnit, UUID> {

    List<GeographicUnit> findByTenantIdAndType(UUID tenantId, GeographicUnitType type);

    List<GeographicUnit> findByParentId(UUID parentId);

    @Query("select g from GeographicUnit g where g.tenantId = :tenantId and g.id in :ids")
    List<GeographicUnit> findAuthorized(UUID tenantId, List<UUID> ids);
}
