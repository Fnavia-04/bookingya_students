package com.project.bookingya.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.bookingya.entities.RoomEntity;

@Repository
public interface IRoomRepository extends JpaRepository<RoomEntity, UUID> {
    Optional<RoomEntity> findByCode(String code);
    boolean existsByCode(String code);
}
