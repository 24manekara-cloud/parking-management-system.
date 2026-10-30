package com.parking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for the daily revenue analytics endpoint.
 */
public record RevenueResponse(
        LocalDate  date,
        BigDecimal totalRevenue,
        String     currency
) {}
