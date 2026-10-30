package com.parking.controller;

import com.parking.dto.*;
import com.parking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for booking lifecycle: create, active listing,
 * checkout, and real-time fare estimation.
 */
@RestController
@RequestMapping("/api/bookings")
@Validated
@Tag(name = "Bookings", description = "Endpoints for vehicle booking lifecycle management")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // ── POST /api/bookings ───────────────────────────────────────────────────

    @PostMapping
    @Operation(
        summary     = "Create a new booking",
        description = "Books an available parking slot for a vehicle. Returns HTTP 201 on success.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content  = @Content(
                mediaType = "application/json",
                schema    = @Schema(implementation = BookingRequest.class),
                examples  = @ExampleObject(
                    name  = "Sample booking",
                    value = """
                            {
                              "vehicleNumber": "MH31AB1234",
                              "slotId": 1
                            }
                            """
                )
            )
        )
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Booking created successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error or vehicle already has an active booking"),
        @ApiResponse(responseCode = "404", description = "Parking slot not found"),
        @ApiResponse(responseCode = "409", description = "Parking slot is currently occupied")
    })
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── GET /api/bookings/active ─────────────────────────────────────────────

    @GetMapping("/active")
    @Operation(
        summary     = "Get all active bookings",
        description = "Returns all vehicles currently parked, with live duration and estimated fee."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Active bookings returned successfully")
    })
    public ResponseEntity<List<ActiveBookingResponse>> getActiveBookings() {
        return ResponseEntity.ok(bookingService.getActiveBookings());
    }

    // ── POST /api/bookings/{id}/checkout ─────────────────────────────────────

    @PostMapping("/{id}/checkout")
    @Operation(
        summary     = "Checkout a vehicle",
        description = "Sets the exit time, calculates the fee, marks payment as PAID, and releases the slot."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Checkout successful with billing details"),
        @ApiResponse(responseCode = "404", description = "Booking not found"),
        @ApiResponse(responseCode = "409", description = "Booking has already been checked out")
    })
    public ResponseEntity<CheckoutResponse> checkout(
            @Parameter(description = "Booking ID", example = "1")
            @PathVariable @Positive(message = "Booking ID must be positive") Long id) {
        return ResponseEntity.ok(bookingService.checkout(id));
    }

    // ── GET /api/bookings/{id}/fare ──────────────────────────────────────────

    @GetMapping("/{id}/fare")
    @Operation(
        summary     = "Get real-time fare estimate",
        description = "Returns the current estimated fee for an active booking without checking out."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Live fare estimate returned"),
        @ApiResponse(responseCode = "404", description = "Booking not found"),
        @ApiResponse(responseCode = "409", description = "Booking has already been checked out")
    })
    public ResponseEntity<FareResponse> getLiveFare(
            @Parameter(description = "Booking ID", example = "1")
            @PathVariable @Positive(message = "Booking ID must be positive") Long id) {
        return ResponseEntity.ok(bookingService.getLiveFare(id));
    }
}
