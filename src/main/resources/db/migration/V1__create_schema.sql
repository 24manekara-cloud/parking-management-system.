-- =================================================================
-- V1__create_schema.sql
-- Smart Parking Management System - Initial Schema
-- =================================================================

-- Parking Slots table
CREATE TABLE IF NOT EXISTS parking_slot (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    slot_number VARCHAR(20)     NOT NULL,
    status      ENUM('AVAILABLE','OCCUPIED') NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT pk_parking_slot PRIMARY KEY (id),
    CONSTRAINT uk_parking_slot_number UNIQUE (slot_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bookings table
CREATE TABLE IF NOT EXISTS booking (
    id             BIGINT          NOT NULL AUTO_INCREMENT,
    vehicle_number VARCHAR(20)     NOT NULL,
    slot_id        BIGINT          NOT NULL,
    entry_time     DATETIME(6)     NOT NULL,
    exit_time      DATETIME(6)     NULL,
    amount         DECIMAL(10, 2)  NULL,
    payment_status ENUM('PENDING','PAID') NOT NULL DEFAULT 'PENDING',
    CONSTRAINT pk_booking PRIMARY KEY (id),
    CONSTRAINT fk_booking_slot FOREIGN KEY (slot_id)
        REFERENCES parking_slot(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Index for common queries
CREATE INDEX idx_booking_slot_id        ON booking (slot_id);
CREATE INDEX idx_booking_vehicle        ON booking (vehicle_number);
CREATE INDEX idx_booking_payment_status ON booking (payment_status);
CREATE INDEX idx_booking_exit_time      ON booking (exit_time);
