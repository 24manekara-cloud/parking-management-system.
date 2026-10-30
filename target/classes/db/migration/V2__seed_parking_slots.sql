-- =================================================================
-- V2__seed_parking_slots.sql
-- Smart Parking Management System - Seed Data
-- =================================================================

-- Insert 10 parking slots (idempotent using INSERT IGNORE)
INSERT IGNORE INTO parking_slot (slot_number, status) VALUES
    ('A-01', 'AVAILABLE'),
    ('A-02', 'AVAILABLE'),
    ('A-03', 'AVAILABLE'),
    ('A-04', 'AVAILABLE'),
    ('A-05', 'AVAILABLE'),
    ('B-01', 'AVAILABLE'),
    ('B-02', 'AVAILABLE'),
    ('B-03', 'AVAILABLE'),
    ('B-04', 'AVAILABLE'),
    ('B-05', 'AVAILABLE');
