package com.politicalops.identity;

/** Mirrors the `role` lookup table (V1 migration) and Table 4 of the HLD. */
public enum RoleCode {
    PLATFORM_ADMIN,
    CONSTITUENCY_ADMIN,
    STRATEGY_ANALYST,
    WAR_ROOM_MANAGER,
    AREA_COORDINATOR,
    BOOTH_COORDINATOR,
    VOLUNTEER,
    DATA_STEWARD,
    CONTENT_REVIEWER,
    AUDITOR
}
