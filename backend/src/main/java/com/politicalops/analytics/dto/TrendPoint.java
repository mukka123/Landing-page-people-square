package com.politicalops.analytics.dto;

import java.math.BigDecimal;
import java.util.UUID;

/** One (election, geography, party) measurement returned by GET /analytics/trends. */
public record TrendPoint(UUID electionId, int electionYear, UUID geographicUnitId,
                          UUID partyId, BigDecimal value, String formulaVersion) {}
