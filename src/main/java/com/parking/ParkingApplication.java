package com.parking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Smart Parking Management System Spring Boot application.
 *
 * <p>Database schema migrations are handled by Flyway on startup.
 * Parking slot seed data is inserted via V2__seed_parking_slots.sql.</p>
 */
@SpringBootApplication
public class ParkingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParkingApplication.class, args);
    }
}
