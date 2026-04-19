package com.project.bookingya.repositories;

import com.project.bookingya.entities.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface IReservationRepository extends JpaRepository<ReservationEntity, UUID> {
    List<ReservationEntity> findByRoomId(UUID roomId);

    List<ReservationEntity> findByGuestId(UUID guestId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
           "FROM ReservationEntity r " +
           "WHERE r.roomId = :roomId " +
           "AND r.checkIn < :checkOut " +
           "AND r.checkOut > :checkIn " +
           "AND (:excludeId IS NULL OR r.id != :excludeId)")
    boolean existsOverlappingReservationForRoom(
            @Param("roomId") UUID roomId,
            @Param("checkIn") LocalDateTime checkIn,
            @Param("checkOut") LocalDateTime checkOut,
            @Param("excludeId") UUID excludeId
    );

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
           "FROM ReservationEntity r " +
           "WHERE r.guestId = :guestId " +
           "AND r.checkIn < :checkOut " +
           "AND r.checkOut > :checkIn " +
           "AND (:excludeId IS NULL OR r.id != :excludeId)")
    boolean existsOverlappingReservationForGuest(
            @Param("guestId") UUID guestId,
            @Param("checkIn") LocalDateTime checkIn,
            @Param("checkOut") LocalDateTime checkOut,
            @Param("excludeId") UUID excludeId
    );
}
