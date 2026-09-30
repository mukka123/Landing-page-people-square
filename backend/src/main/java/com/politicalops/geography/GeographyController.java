package com.politicalops.geography;

import com.politicalops.common.TenantContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/geography")
public class GeographyController {

    private final GeographicUnitRepository repository;

    public GeographyController(GeographicUnitRepository repository) {
        this.repository = repository;
    }

    /**
     * Returns only geography the caller is authorized for — geography scope always comes
     * from TenantContext (derived server-side from the token), never from a request param.
     */
    @GetMapping("/units")
    public List<GeographicUnit> listAuthorizedUnits() {
        TenantContext ctx = TenantContext.current();
        return repository.findAuthorized(ctx.tenantId(), List.copyOf(ctx.authorizedGeographicUnitIds()));
    }

    @GetMapping("/units/{parentId}/children")
    public List<GeographicUnit> children(@PathVariable java.util.UUID parentId) {
        return repository.findByParentId(parentId);
    }
}
