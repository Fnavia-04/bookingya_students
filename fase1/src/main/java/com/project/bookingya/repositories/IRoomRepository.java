package com.project.bookingya.repositories;

import com.project.bookingya.entities.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IRoomRepository extends JpaRepository<RoomEntity, UUID> {
}
