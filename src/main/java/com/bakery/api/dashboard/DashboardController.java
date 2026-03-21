package com.bakery.api.dashboard;

import com.bakery.api.dashboard.dto.DailySalesResponse;
import com.bakery.api.dashboard.dto.DashboardSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@Tag(name = "Dashboard", description = "Business metrics for the client dashboard (ADMIN only)")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Dashboard summary", description = "High-level business metrics (counts and revenue).")
    public ResponseEntity<DashboardSummaryResponse> summary() {
        return ResponseEntity.ok(dashboardService.summary());
    }

    @GetMapping("/sales/daily")
    @Operation(summary = "Daily sales", description = "Paid purchases grouped by day for the last N days (inclusive).")
    public ResponseEntity<DailySalesResponse> dailySales(
            @RequestParam(name = "days", defaultValue = "30") int days
    ) {
        return ResponseEntity.ok(dashboardService.dailySales(days));
    }
}
