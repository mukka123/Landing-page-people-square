package com.politicalops.common;

import java.util.Set;
import java.util.UUID;

/**
 * Carries the authenticated requests tenant, role set and authorized geographic scope
 * through a single request. Populated by com.politicalops.config.SecurityConfig from the
 * validated OIDC token claims — NEVER trust a tenant/geography value from a request body
 * or query param; the HLD is explicit that "hiding controls in the UI is not an access
 * control" and the same principle applies to client-supplied scope claims.
 */
public final class TenantContext {

    private static final ThreadLocal<TenantContext> CURRENT = new ThreadLocal<>();

    private final UUID tenantId;
    private final UUID userId;
    private final Set<String> roleCodes;
    private final Set<UUID> authorizedGeographicUnitIds;

    public TenantContext(UUID tenantId, UUID userId, Set<String> roleCodes,
                          Set<UUID> authorizedGeographicUnitIds) {
        this.tenantId = tenantId;
        this.userId = userId;
        this.roleCodes = roleCodes;
        this.authorizedGeographicUnitIds = authorizedGeographicUnitIds;
    }

    public static void set(TenantContext ctx) { CURRENT.set(ctx); }

    public static TenantContext current() {
        TenantContext ctx = CURRENT.get();
        if (ctx == null) {
            throw new IllegalStateException("No TenantContext bound to this request thread");
        }
        return ctx;
    }

    public static void clear() { CURRENT.remove(); }

    public UUID tenantId() { return tenantId; }
    public UUID userId() { return userId; }
    public boolean hasRole(String code) { return roleCodes.contains(code); }

    /** Every geography-scoped query MUST intersect with this set server-side. */
    public Set<UUID> authorizedGeographicUnitIds() { return authorizedGeographicUnitIds; }
}
