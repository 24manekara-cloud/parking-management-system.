package com.parking.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity representing a vehicle's parking booking.
 * Tracks entry/exit times, billed amount, and payment status.
 */
@Entity
@Table(
    name = "booking",
    indexes = {
        @Index(name = "idx_booking_slot_id",        columnList = "slot_id"),
        @Index(name = "idx_booking_vehicle",        columnList = "vehicle_number"),
        @Index(name = "idx_booking_payment_status", columnList = "payment_status"),
        @Index(name = "idx_booking_exit_time",      columnList = "exit_time")
    }
)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_number", nullable = false, length = 20)
    private String vehicleNumber;

    /**
     * The slot this booking is associated with.
     * LAZY loading is used; the slot is fetched on demand.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slot_id", nullable = false, foreignKey = @ForeignKey(name = "fk_booking_slot"))
    private ParkingSlot slot;

    @Column(name = "entry_time", nullable = false)
    private LocalDateTime entryTime;

    @Column(name = "exit_time")
    private LocalDateTime exitTime;

    /** Total billed amount (set at checkout). Null until checkout is performed. */
    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 10)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    // ── Constructors ─────────────────────────────────────────────────────────

    public Booking() {}

    // ── Getters & Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public ParkingSlot getSlot() { return slot; }
    public void setSlot(ParkingSlot slot) { this.slot = slot; }

    public LocalDateTime getEntryTime() { return entryTime; }
    public void setEntryTime(LocalDateTime entryTime) { this.entryTime = entryTime; }

    public LocalDateTime getExitTime() { return exitTime; }
    public void setExitTime(LocalDateTime exitTime) { this.exitTime = exitTime; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    // ── Domain Methods ────────────────────────────────────────────────────────

    /**
     * Returns {@code true} if the vehicle has already checked out.
     */
    public boolean isCheckedOut() {
        return this.exitTime != null;
    }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Long id;
        private String vehicleNumber;
        private ParkingSlot slot;
        private LocalDateTime entryTime;
        private LocalDateTime exitTime;
        private BigDecimal amount;
        private PaymentStatus paymentStatus = PaymentStatus.PENDING;

        public Builder id(Long id)                           { this.id = id; return this; }
        public Builder vehicleNumber(String vn)              { this.vehicleNumber = vn; return this; }
        public Builder slot(ParkingSlot slot)                { this.slot = slot; return this; }
        public Builder entryTime(LocalDateTime et)           { this.entryTime = et; return this; }
        public Builder exitTime(LocalDateTime xt)            { this.exitTime = xt; return this; }
        public Builder amount(BigDecimal amount)             { this.amount = amount; return this; }
        public Builder paymentStatus(PaymentStatus ps)       { this.paymentStatus = ps; return this; }

        public Booking build() {
            Booking b = new Booking();
            b.id = this.id;
            b.vehicleNumber = this.vehicleNumber;
            b.slot = this.slot;
            b.entryTime = this.entryTime;
            b.exitTime = this.exitTime;
            b.amount = this.amount;
            b.paymentStatus = this.paymentStatus;
            return b;
        }
    }
}
