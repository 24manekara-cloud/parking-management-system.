package com.parking.controller;

import com.parking.dto.RevenueResponse;
import com.parking.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * REST controller for revenue analytics.
 */
@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Endpoints for revenue and usage analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    // ── GET /api/analytics/revenue?date=YYYY-MM-DD ───────────────────────────

    @GetMapping("/revenue")
    @Operation(
        summary     = "Get daily revenue",
        description = "Calculates the total revenue for the specified date. "
                    + "Only counts completed (PAID) bookings with exit time on that date."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Daily revenue calculated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date format — must be ISO-8601 (YYYY-MM-DD)")
    })
    public ResponseEntity<RevenueResponse> getDailyRevenue(
            @Parameter(
                description = "Target date in ISO-8601 format (YYYY-MM-DD)",
                example     = "2026-10-30",
                required    = true
            )
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        return ResponseEntity.ok(analyticsService.getDailyRevenue(date));
    }
}
