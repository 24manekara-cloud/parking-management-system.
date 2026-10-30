package com.parking.service;

import com.parking.exception.InvalidBookingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link BillingService}.
 *
 * Covers all prescribed pricing rules:
 *   ₹50 for every 3 hours or fraction thereof.
 *   Fee = CEILING(durationSeconds / 10800) × 50
 */
@DisplayName("BillingService — Fee Calculation Tests")
class BillingServiceTest {

    private BillingService billingService;

    /** Fixed anchor timestamp used as entry time in all tests. */
    private static final LocalDateTime ENTRY = LocalDateTime.of(2026, 10, 30, 10, 0, 0);

    @BeforeEach
    void setUp() {
        billingService = new BillingService();
    }

    // ── Duration → Expected fee test cases ──────────────────────────────────

    @Test
    @DisplayName("30 minutes → ₹50")
    void thirtyMinutes_shouldReturn50() {
        LocalDateTime exit = ENTRY.plusMinutes(30);
        assertFee(exit, "50");
    }

    @Test
    @DisplayName("2 hours → ₹50")
    void twoHours_shouldReturn50() {
        LocalDateTime exit = ENTRY.plusHours(2);
        assertFee(exit, "50");
    }

    @Test
    @DisplayName("2 hours 15 minutes → ₹50")
    void twoHoursFifteenMinutes_shouldReturn50() {
        LocalDateTime exit = ENTRY.plusHours(2).plusMinutes(15);
        assertFee(exit, "50");
    }

    @Test
    @DisplayName("Exactly 3 hours → ₹50")
    void exactlyThreeHours_shouldReturn50() {
        LocalDateTime exit = ENTRY.plusHours(3);
        assertFee(exit, "50");
    }

    @Test
    @DisplayName("3 hours 5 minutes → ₹100")
    void threeHoursFiveMinutes_shouldReturn100() {
        LocalDateTime exit = ENTRY.plusHours(3).plusMinutes(5);
        assertFee(exit, "100");
    }

    @Test
    @DisplayName("Exactly 6 hours → ₹100")
    void exactlySixHours_shouldReturn100() {
        LocalDateTime exit = ENTRY.plusHours(6);
        assertFee(exit, "100");
    }

    @Test
    @DisplayName("6 hours 1 minute → ₹150")
    void sixHoursOneMinute_shouldReturn150() {
        LocalDateTime exit = ENTRY.plusHours(6).plusMinutes(1);
        assertFee(exit, "150");
    }

    @Test
    @DisplayName("9 hours → ₹150")
    void nineHours_shouldReturn150() {
        LocalDateTime exit = ENTRY.plusHours(9);
        assertFee(exit, "150");
    }

    @Test
    @DisplayName("1 second beyond 3 hours → ₹100")
    void oneSecondBeyondThreeHours_shouldReturn100() {
        LocalDateTime exit = ENTRY.plusHours(3).plusSeconds(1);
        assertFee(exit, "100");
    }

    @Test
    @DisplayName("Zero duration (entry == exit) → ₹0")
    void zeroDuration_shouldReturnZero() {
        BigDecimal fee = billingService.calculateFee(ENTRY, ENTRY);
        assertThat(fee).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ── Exception cases ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Exit time before entry time → InvalidBookingException")
    void exitBeforeEntry_shouldThrowInvalidBookingException() {
        LocalDateTime exit = ENTRY.minusHours(1);
        assertThatThrownBy(() -> billingService.calculateFee(ENTRY, exit))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessageContaining("Exit time");
    }

    @Test
    @DisplayName("Null entry time → InvalidBookingException")
    void nullEntryTime_shouldThrowInvalidBookingException() {
        assertThatThrownBy(() -> billingService.calculateFee(null, ENTRY))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessageContaining("Entry time must not be null");
    }

    @Test
    @DisplayName("Null exit time → InvalidBookingException")
    void nullExitTime_shouldThrowInvalidBookingException() {
        assertThatThrownBy(() -> billingService.calculateFee(ENTRY, null))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessageContaining("Exit time must not be null");
    }

    // ── formatDuration helper ────────────────────────────────────────────────

    @Test
    @DisplayName("formatDuration: 90 minutes → '1h 30m 0s'")
    void formatDuration_ninetyMinutes() {
        LocalDateTime exit = ENTRY.plusMinutes(90);
        String result = billingService.formatDuration(ENTRY, exit);
        assertThat(result).isEqualTo("1h 30m 0s");
    }

    @Test
    @DisplayName("formatDuration: null entry → 'N/A'")
    void formatDuration_nullEntry_returnsNA() {
        String result = billingService.formatDuration(null, ENTRY);
        assertThat(result).isEqualTo("N/A");
    }

    // ── Private helper ───────────────────────────────────────────────────────

    private void assertFee(LocalDateTime exit, String expectedAmount) {
        BigDecimal fee = billingService.calculateFee(ENTRY, exit);
        assertThat(fee).isEqualByComparingTo(new BigDecimal(expectedAmount));
    }
}
