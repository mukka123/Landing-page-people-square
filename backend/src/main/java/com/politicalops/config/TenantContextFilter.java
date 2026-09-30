package com.politicalops.config;

import com.politicalops.common.TenantContext;
import com.politicalops.identity.RoleCode;
import com.politicalops.identity.UserRoleAssignment;
import com.politicalops.identity.UserRoleAssignmentRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Runs after Spring Security has validated the JWT and BEFORE the controller. Reads
 * tenant/user claims from the *validated* token — never from a request body/query param —
 * and expands the users active role assignments into an authorized-geography set that
 * every downstream query (GeographyController, AnalyticsService, ...) intersects against.
 */
@Component
@Order(100)
public class TenantContextFilter extends OncePerRequestFilter {

    private final UserRoleAssignmentRepository roleAssignmentRepository;

    public TenantContextFilter(UserRoleAssignmentRepository roleAssignmentRepository) {
        this.roleAssignmentRepository = roleAssignmentRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain chain) throws ServletException, IOException {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
                UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenant_id"));
                UUID userId = UUID.fromString(jwt.getClaimAsString("sub"));

                var assignments = roleAssignmentRepository.findByUserId(userId).stream()
                        .filter(UserRoleAssignment::isActive)
                        .toList();

                Set<String> roles = assignments.stream()
                        .map(a -> a.getRoleCode().name())
                        .collect(Collectors.toSet());

                boolean isPlatformAdmin = roles.contains(RoleCode.PLATFORM_ADMIN.name());
                Set<UUID> authorizedGeo = isPlatformAdmin
                        ? Set.of() // platform admin bypasses geography scoping entirely — handled explicitly, not via this set
                        : assignments.stream()
                            .map(UserRoleAssignment::getGeographicUnitId)
                            .filter(java.util.Objects::nonNull)
                            .collect(Collectors.toSet());

                TenantContext.set(new TenantContext(tenantId, userId, roles, authorizedGeo));
            }
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
