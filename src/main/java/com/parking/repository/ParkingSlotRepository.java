package com.parking.repository;

import com.parking.model.ParkingSlot;
import com.parking.model.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link ParkingSlot} entities.
 */
@Repository
public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, Long> {

    /**
     * Returns all slots matching the given status.
     */
    List<ParkingSlot> findByStatus(SlotStatus status);

    /**
     * Fetches a slot by ID with a pessimistic write lock to prevent
     * concurrent booking of the same slot (race-condition guard).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ParkingSlot s WHERE s.id = :id")
    Optional<ParkingSlot> findByIdWithLock(@Param("id") Long id);

    /**
     * Checks whether a slot with the given slot number already exists.
     */
    boolean existsBySlotNumber(String slotNumber);
}
