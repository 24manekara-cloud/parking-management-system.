# 🚗 Smart Parking Management System

A production-grade **Spring Boot REST API** for managing parking slots, vehicle bookings, real-time fare estimation, and daily revenue analytics.

---

## 📋 Table of Contents

- [Project Overview](#-project-overview)
- [Features](#-features)
- [Technologies Used](#-technologies-used)
- [Architecture](#-architecture)
- [Database Setup](#-database-setup)
- [Environment Variables](#-environment-variables)
- [Build & Run](#-build--run)
- [API Endpoints](#-api-endpoints)
- [Fee Calculation](#-fee-calculation)
- [Testing](#-testing)
- [Example Requests & Responses](#-example-requests--responses)
- [Swagger UI](#-swagger-ui)

---

## 🏗 Project Overview

The Smart Parking Management System provides a RESTful backend for:

- Viewing real-time parking slot availability
- Booking slots for vehicles with race-condition prevention
- Checking out vehicles with automatic fee calculation
- Getting live fare estimates without ending the booking
- Querying daily revenue with database-level aggregation

---

## ✨ Features

| Feature | Description |
|---|---|
| **Slot Availability** | View all or only available slots |
| **Vehicle Booking** | Book a slot with Indian vehicle number validation |
| **Race Condition Prevention** | Pessimistic DB locking prevents double-booking |
| **Checkout & Billing** | Automatic exit time, fee calculation, slot release |
| **Live Fare Estimate** | Query current fee without checking out |
| **Daily Revenue Analytics** | DB-aggregate query for revenue per day |
| **Global Exception Handling** | Standardized error responses |
| **Swagger UI** | Interactive API documentation |
| **Flyway Migrations** | Version-controlled schema + seed data |
| **Unit Tests** | JUnit 5 + Mockito test coverage |

---

## 🛠 Technologies Used

| Technology | Version |
|---|---|
| Java | 21 |
| Spring Boot | 3.3.5 |
| Spring Data JPA | 3.3.5 |
| Spring Validation | 3.3.5 |
| Hibernate | 6.x |
| MySQL | 8.x |
| Flyway | 10.x |
| Lombok | Latest |
| Springdoc OpenAPI | 2.6.0 |
| JUnit 5 | 5.x |
| Mockito | 5.x |
| H2 (test) | 2.x |

---

## 🏛 Architecture

```
com.parking
├── ParkingApplication.java          ← Spring Boot entry point
│
├── controller/
│   ├── ParkingSlotController.java   ← GET /api/slots/**
│   ├── BookingController.java       ← POST|GET /api/bookings/**
│   └── AnalyticsController.java     ← GET /api/analytics/**
│
├── service/
│   ├── ParkingSlotService.java      ← Slot retrieval logic
│   ├── BookingService.java          ← Booking lifecycle logic
│   ├── BillingService.java          ← Fee calculation
│   └── AnalyticsService.java        ← Revenue analytics
│
├── repository/
│   ├── ParkingSlotRepository.java   ← JPA + pessimistic lock query
│   └── BookingRepository.java       ← Active bookings + revenue aggregate
│
├── model/
│   ├── ParkingSlot.java             ← @Entity
│   ├── Booking.java                 ← @Entity with @ManyToOne
│   ├── SlotStatus.java              ← AVAILABLE | OCCUPIED
│   └── PaymentStatus.java           ← PENDING | PAID
│
├── dto/
│   ├── BookingRequest.java          ← POST /api/bookings payload
│   ├── BookingResponse.java         ← Booking creation response
│   ├── ActiveBookingResponse.java   ← Active booking with live data
│   ├── CheckoutResponse.java        ← Checkout billing details
│   ├── FareResponse.java            ← Live fare estimate
│   ├── SlotResponse.java            ← Slot details
│   └── RevenueResponse.java         ← Daily revenue
│
├── exception/
│   ├── ResourceNotFoundException.java
│   ├── SlotUnavailableException.java
│   ├── BookingAlreadyCompletedException.java
│   ├── InvalidBookingException.java
│   └── GlobalExceptionHandler.java  ← @RestControllerAdvice
│
└── config/
    └── OpenApiConfig.java           ← Swagger/OpenAPI configuration
```

**Request Flow:**
```
HTTP Request → Controller → Service → Repository → Database
                                ↓
                          BillingService (fee calculation)
                                ↓
              DTO ← Service ← Controller → HTTP Response
```

---

## 🐳 Running with Docker (Recommended)

Docker Compose configuration is included in the project:
- **`mysql`**: MySQL 8.0 on port `3306` with health check and persistent volume.
- **`app`**: Spring Boot application running on port `8080`.

### Option A: 1-Click Startup (Both MySQL & App in Docker)
Double-click `start-all.bat` or run in terminal:
```powershell
docker compose up --build -d
```
Access Swagger UI at: `http://localhost:8080/swagger-ui/index.html`

### Option B: Run Only MySQL in Docker (Develop locally)
Double-click `start-mysql-only.bat` or run in terminal:
```powershell
docker compose up -d mysql
```
Then start the application locally with your IDE or:
```powershell
$env:DB_USER = "root"
$env:DB_PASSWORD = "rootpassword"
.\mvnw.cmd spring-boot:run
```

To stop containers:
```powershell
docker compose down
```

---

## 🗄 Database Setup (Manual, without Docker)

### Prerequisites
- MySQL 8.x running on `localhost:3306`

### Create the database

```sql
CREATE DATABASE IF NOT EXISTS parking_management
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'parking_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON parking_management.* TO 'parking_user'@'localhost';
FLUSH PRIVILEGES;
```

> **Note:** Flyway will automatically create the schema and seed 10 parking slots on first startup.

### Schema (auto-created by Flyway)

**`parking_slot` table:**
| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT |
| slot_number | VARCHAR(20) | NOT NULL, UNIQUE |
| status | ENUM | NOT NULL, DEFAULT 'AVAILABLE' |

**`booking` table:**
| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT |
| vehicle_number | VARCHAR(20) | NOT NULL |
| slot_id | BIGINT | FK → parking_slot.id |
| entry_time | DATETIME(6) | NOT NULL |
| exit_time | DATETIME(6) | NULLABLE |
| amount | DECIMAL(10,2) | NULLABLE |
| payment_status | ENUM | NOT NULL, DEFAULT 'PENDING' |

---

## ⚙ Environment Variables

Configure these environment variables before running the application:

| Variable | Default | Description |
|---|---|---|
| `DB_HOST` | `localhost` | MySQL host |
| `DB_PORT` | `3306` | MySQL port |
| `DB_NAME` | `parking_management` | Database name |
| `DB_USER` | `root` | MySQL username |
| `DB_PASSWORD` | *(empty)* | MySQL password |
| `SERVER_PORT` | `8080` | Application HTTP port |

### Setting environment variables

**Windows (PowerShell):**
```powershell
$env:DB_HOST     = "localhost"
$env:DB_PORT     = "3306"
$env:DB_NAME     = "parking_management"
$env:DB_USER     = "parking_user"
$env:DB_PASSWORD = "your_password"
```

**Linux/macOS:**
```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=parking_management
export DB_USER=parking_user
export DB_PASSWORD=your_password
```

---

## 🚀 Build & Run

### Prerequisites
- Java 21+ (JDK)
- MySQL 8.x
- Maven 3.9+ **or** use the included Maven Wrapper (`mvnw.cmd` / `mvnw`)

### Using Maven Wrapper (recommended — no Maven install needed)

**Windows:**
```powershell
# Set environment variables first (see above)
# Then run from project root:
.\mvnw.cmd spring-boot:run
```

**Linux/macOS:**
```bash
chmod +x mvnw
./mvnw spring-boot:run
```

### Using system Maven

```bash
mvn spring-boot:run
```

### Build JAR and run

```bash
mvn clean package -DskipTests
java -jar target/smart-parking-management-1.0.0.jar
```

### Run with custom DB credentials inline

```powershell
.\mvnw.cmd spring-boot:run `
  -Dspring-boot.run.jvmArguments="-DDB_USER=root -DDB_PASSWORD=secret"
```

---

## 📡 API Endpoints

Base URL: `http://localhost:8080`

### Parking Slots

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/slots` | Get all parking slots |
| GET | `/api/slots/available` | Get only available slots |

### Bookings

| Method | Endpoint | Description | Status |
|---|---|---|---|
| POST | `/api/bookings` | Create a booking | 201 |
| GET | `/api/bookings/active` | Get all active bookings | 200 |
| POST | `/api/bookings/{id}/checkout` | Checkout a vehicle | 200 |
| GET | `/api/bookings/{id}/fare` | Get live fare estimate | 200 |

### Analytics

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/analytics/revenue?date=YYYY-MM-DD` | Get daily revenue |

---

## 💰 Fee Calculation

**Policy:** ₹50 per every 3 hours (or fraction thereof).

**Formula:**
```
Fee = CEILING(DurationSeconds / 10800) × ₹50
```

| Duration | Fee |
|---|---|
| 1 second – 3 hours | ₹50 |
| 3 hours 1 second – 6 hours | ₹100 |
| 6 hours 1 second – 9 hours | ₹150 |
| 0 seconds (exact) | ₹0 |

**Examples:**
```
30 minutes       → ₹50
2h 15m           → ₹50
Exactly 3h       → ₹50
3h 5m            → ₹100
Exactly 6h       → ₹100
6h 1m            → ₹150
9h               → ₹150
```

---

## 🧪 Testing

### Run all unit tests

```bash
mvn test
# or
.\mvnw.cmd test
```

### Test classes

| Test Class | Coverage |
|---|---|
| `BillingServiceTest` | 13 test cases for all fee calculation scenarios |
| `BookingServiceTest` | 9 test cases for booking lifecycle |

### BillingService test cases

1. 30 minutes → ₹50
2. 2 hours → ₹50
3. 2h 15m → ₹50
4. Exactly 3h → ₹50
5. 3h 5m → ₹100
6. Exactly 6h → ₹100
7. 6h 1m → ₹150
8. 9h → ₹150
9. 1 second beyond 3h → ₹100
10. Zero duration → ₹0
11. Exit before entry → `InvalidBookingException`
12. Null entry time → `InvalidBookingException`
13. Null exit time → `InvalidBookingException`

### BookingService test cases

1. Successful booking
2. Book an occupied slot → `SlotUnavailableException`
3. Book nonexistent slot → `ResourceNotFoundException`
4. Vehicle already has active booking → `InvalidBookingException`
5. Successful checkout
6. Checkout nonexistent booking → `ResourceNotFoundException`
7. Double checkout → `BookingAlreadyCompletedException`
8. Get active bookings — returns live data
9. Get active bookings — empty list

---

## 📝 Example Requests & Responses

### Create Booking

**Request:**
```http
POST /api/bookings
Content-Type: application/json

{
  "vehicleNumber": "MH31AB1234",
  "slotId": 1
}
```

**Response (201 Created):**
```json
{
  "bookingId": 1,
  "vehicleNumber": "MH31AB1234",
  "slotId": 1,
  "slotNumber": "A-01",
  "entryTime": "2026-10-30T20:30:00",
  "exitTime": null,
  "amount": null,
  "paymentStatus": "PENDING"
}
```

### Checkout

**Request:**
```http
POST /api/bookings/1/checkout
```

**Response (200 OK):**
```json
{
  "bookingId": 1,
  "vehicleNumber": "MH31AB1234",
  "slotNumber": "A-01",
  "entryTime": "2026-10-30T20:30:00",
  "exitTime": "2026-10-30T21:15:00",
  "totalDuration": "0h 45m 0s",
  "totalAmount": 50.00,
  "paymentStatus": "PAID",
  "message": "Checkout successful. Slot A-01 is now available."
}
```

### Live Fare Estimate

**Request:**
```http
GET /api/bookings/1/fare
```

**Response (200 OK):**
```json
{
  "bookingId": 1,
  "vehicleNumber": "MH31AB1234",
  "slotNumber": "A-01",
  "currentDuration": "2h 10m 30s",
  "estimatedFare": 50.00
}
```

### Daily Revenue

**Request:**
```http
GET /api/analytics/revenue?date=2026-10-30
```

**Response (200 OK):**
```json
{
  "date": "2026-10-30",
  "totalRevenue": 350.00,
  "currency": "INR"
}
```

### Error Response Format

```json
{
  "timestamp": "2026-10-30T20:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Parking slot 'A-01' is already occupied. Please choose a different slot."
}
```

---

## 📖 Swagger UI

Once the application is running, access the interactive API documentation at:

```
http://localhost:8080/swagger-ui/index.html
```

OpenAPI spec (JSON):
```
http://localhost:8080/api-docs
```

---

## 📦 Postman Collection

Import `SmartParking.postman_collection.json` into Postman for pre-configured requests with automated test scripts.

Set the `baseUrl` collection variable to `http://localhost:8080` (default).

---

## 🔒 Security Notes

- Database credentials are read from environment variables — never hardcoded.
- Internal stack traces and SQL errors are never exposed in API responses.
- All error responses follow the standardized error envelope.

---

## 📁 Project Structure Summary

```
smart-parking-management/
├── .mvn/wrapper/
│   ├── maven-wrapper.jar
│   └── maven-wrapper.properties
├── src/
│   ├── main/
│   │   ├── java/com/parking/
│   │   │   ├── ParkingApplication.java
│   │   │   ├── config/OpenApiConfig.java
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── exception/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/
│   │           ├── V1__create_schema.sql
│   │           └── V2__seed_parking_slots.sql
│   └── test/
│       ├── java/com/parking/service/
│       │   ├── BillingServiceTest.java
│       │   └── BookingServiceTest.java
│       └── resources/application.yml
├── SmartParking.postman_collection.json
├── mvnw.cmd
├── pom.xml
└── README.md
```
"# Parking Management System" 
