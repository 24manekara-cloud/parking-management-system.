package com.parking.controller;

import com.parking.dto.SlotResponse;
import com.parking.service.ParkingSlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller exposing parking slot availability endpoints.
 */
@RestController
@RequestMapping("/api/slots")
@Tag(name = "Parking Slots", description = "Endpoints for viewing parking slot availability")
public class ParkingSlotController {

    private final ParkingSlotService parkingSlotService;

    public ParkingSlotController(ParkingSlotService parkingSlotService) {
        this.parkingSlotService = parkingSlotService;
    }

    // ── GET /api/slots ───────────────────────────────────────────────────────

    @GetMapping
    @Operation(
        summary     = "Get all parking slots",
        description = "Returns all parking slots in the system, regardless of their current status."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of all parking slots returned successfully")
    })
    public ResponseEntity<List<SlotResponse>> getAllSlots() {
        return ResponseEntity.ok(parkingSlotService.getAllSlots());
    }

    // ── GET /api/slots/available ─────────────────────────────────────────────

    @GetMapping("/available")
    @Operation(
        summary     = "Get available parking slots",
        description = "Returns only the parking slots that are currently AVAILABLE for booking."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of available slots returned successfully")
    })
    public ResponseEntity<List<SlotResponse>> getAvailableSlots() {
        return ResponseEntity.ok(parkingSlotService.getAvailableSlots());
    }
}
