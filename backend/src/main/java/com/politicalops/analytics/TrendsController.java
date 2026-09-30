package com.politicalops.analytics;

import com.politicalops.analytics.dto.TrendPoint;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
public class TrendsController {

    private final AnalyticsService analyticsService;

    public TrendsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /** GET /api/v1/analytics/trends?geographicUnitIds=...&metric=VOTE_SHARE */
    @GetMapping("/trends")
    public List<TrendPoint> trends(@RequestParam List<UUID> geographicUnitIds,
                                    @RequestParam Metric metric) {
        return analyticsService.trends(geographicUnitIds, metric);
    }
}
