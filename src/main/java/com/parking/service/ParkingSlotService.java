package com.parking.service;

import com.parking.dto.SlotResponse;
import com.parking.model.ParkingSlot;
import com.parking.model.SlotStatus;
import com.parking.repository.ParkingSlotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for parking slot management.
 */
@Service
@Transactional(readOnly = true)
public class ParkingSlotService {

    private static final Logger log = LoggerFactory.getLogger(ParkingSlotService.class);

    private final ParkingSlotRepository slotRepository;

    public ParkingSlotService(ParkingSlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    /**
     * Returns all parking slots regardless of status.
     */
    public List<SlotResponse> getAllSlots() {
        log.debug("Fetching all parking slots");
        return slotRepository.findAll()
                .stream()
                .map(this::toSlotResponse)
                .toList();
    }

    /**
     * Returns only currently available parking slots.
     */
    public List<SlotResponse> getAvailableSlots() {
        log.debug("Fetching available parking slots");
        return slotRepository.findByStatus(SlotStatus.AVAILABLE)
                .stream()
                .map(this::toSlotResponse)
                .toList();
    }

    // ── Mapper ───────────────────────────────────────────────────────────────

    public SlotResponse toSlotResponse(ParkingSlot slot) {
        return new SlotResponse(slot.getId(), slot.getSlotNumber(), slot.getStatus());
    }
}
