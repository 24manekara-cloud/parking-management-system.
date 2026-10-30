package com.parking.model;

import jakarta.persistence.*;

/**
 * JPA entity representing a physical parking slot in the lot.
 * Each slot has a unique human-readable slot number and a current status.
 */
@Entity
@Table(
    name = "parking_slot",
    uniqueConstraints = @UniqueConstraint(name = "uk_parking_slot_number", columnNames = "slot_number")
)
public class ParkingSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slot_number", nullable = false, length = 20)
    private String slotNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private SlotStatus status = SlotStatus.AVAILABLE;

    // ── Constructors ─────────────────────────────────────────────────────────

    public ParkingSlot() {}

    public ParkingSlot(Long id, String slotNumber, SlotStatus status) {
        this.id = id;
        this.slotNumber = slotNumber;
        this.status = status;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSlotNumber() { return slotNumber; }
    public void setSlotNumber(String slotNumber) { this.slotNumber = slotNumber; }

    public SlotStatus getStatus() { return status; }
    public void setStatus(SlotStatus status) { this.status = status; }

    // ── Domain Methods ────────────────────────────────────────────────────────

    /**
     * Returns {@code true} if this slot is available for booking.
     */
    public boolean isAvailable() {
        return SlotStatus.AVAILABLE == this.status;
    }

    /**
     * Marks the slot as occupied.
     */
    public void occupy() {
        this.status = SlotStatus.OCCUPIED;
    }

    /**
     * Releases the slot back to available.
     */
    public void release() {
        this.status = SlotStatus.AVAILABLE;
    }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Long id;
        private String slotNumber;
        private SlotStatus status = SlotStatus.AVAILABLE;

        public Builder id(Long id)                 { this.id = id; return this; }
        public Builder slotNumber(String sn)       { this.slotNumber = sn; return this; }
        public Builder status(SlotStatus s)        { this.status = s; return this; }

        public ParkingSlot build() {
            ParkingSlot ps = new ParkingSlot();
            ps.id = this.id;
            ps.slotNumber = this.slotNumber;
            ps.status = this.status;
            return ps;
        }
    }
}
