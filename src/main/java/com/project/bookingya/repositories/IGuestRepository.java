package com.project.bookingya.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.bookingya.entities.GuestEntity;

@Repository
public interface IGuestRepository extends JpaRepository<GuestEntity, UUID> {
    Optional<GuestEntity> findByIdentification(String identification);
    boolean existsByIdentification(String identification);
    boolean existsByEmail(String email);
}
