package com.project.bookingya.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.bookingya.entities.ReservationEntity;

@Repository
public interface IReservationRepository extends JpaRepository<ReservationEntity, UUID> {

    List<ReservationEntity> findByRoomId(UUID roomId);

    List<ReservationEntity> findByGuestId(UUID guestId);

    @Query("""
        select (count(r) > 0) from ReservationEntity r
        where r.roomId = :roomId
          and (:excludeId is null or r.id <> :excludeId)
          and r.checkIn < :checkOut
          and r.checkOut > :checkIn
        """)
    boolean existsOverlappingReservationForRoom(@Param("roomId") UUID roomId, @Param("checkIn") LocalDateTime checkIn, @Param("checkOut") LocalDateTime checkOut, @Param("excludeId") UUID excludeId);

    @Query("""
        select (count(r) > 0) from ReservationEntity r
        where r.guestId = :guestId
          and (:excludeId is null or r.id <> :excludeId)
          and r.checkIn < :checkOut
          and r.checkOut > :checkIn
        """)
    boolean existsOverlappingReservationForGuest(@Param("guestId") UUID guestId, @Param("checkIn") LocalDateTime checkIn, @Param("checkOut") LocalDateTime checkOut, @Param("excludeId") UUID excludeId);
}
