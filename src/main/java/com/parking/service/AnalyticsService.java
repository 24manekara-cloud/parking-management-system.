package com.parking.service;

import com.parking.dto.RevenueResponse;
import com.parking.model.PaymentStatus;
import com.parking.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Service for revenue analytics.
 * Delegates the summation to the database using an aggregate query rather
 * than fetching and summing records in application memory.
 */
@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final BookingRepository bookingRepository;

    public AnalyticsService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    /**
     * Returns total revenue for the specified date, counting only bookings
     * whose {@code exitTime} falls within that calendar day and whose
     * {@code paymentStatus} is {@link PaymentStatus#PAID}.
     *
     * @param date the target calendar date
     * @return a {@link RevenueResponse} with the aggregated amount
     */
    public RevenueResponse getDailyRevenue(LocalDate date) {
        log.debug("Calculating revenue for date {}", date);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay   = date.atTime(LocalTime.MAX);

        BigDecimal total = bookingRepository.sumRevenueByDateAndStatus(
                startOfDay, endOfDay, PaymentStatus.PAID);

        // COALESCE in SQL returns 0, but guard against null just in case
        if (total == null) {
            total = BigDecimal.ZERO;
        }

        log.info("Revenue for {} = ₹{}", date, total);
        return new RevenueResponse(date, total, "INR");
    }
}
