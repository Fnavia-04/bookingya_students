package com.project.bookingya.controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.project.bookingya.dtos.ReservationDto;
import com.project.bookingya.models.Reservation;
import com.project.bookingya.services.ReservationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Reservations", description = "Reservation related operations")
@Validated
@RestController
@RequestMapping("/reservation")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Operation(summary = "Get all reservations")
    @GetMapping
    public ResponseEntity<List<Reservation>> getAll() {
        return ResponseEntity.ok(reservationService.getAll());
    }

    @Operation(summary = "Get a reservation by UUID")
    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(reservationService.getById(id));
    }

    @Operation(summary = "Get reservations by room")
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<Reservation>> getByRoomId(@PathVariable UUID roomId) {
        return ResponseEntity.ok(reservationService.getByRoomId(roomId));
    }

    @Operation(summary = "Get reservations by guest")
    @GetMapping("/guest/{guestId}")
    public ResponseEntity<List<Reservation>> getByGuestId(@PathVariable UUID guestId) {
        return ResponseEntity.ok(reservationService.getByGuestId(guestId));
    }

    @Operation(summary = "Check room availability")
    @GetMapping("/availability/room/{roomId}")
    public ResponseEntity<Map<String, Boolean>> isRoomAvailable(
        @PathVariable UUID roomId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkIn,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkOut
    ) {
        boolean available = reservationService.isRoomAvailable(roomId, checkIn, checkOut);
        return ResponseEntity.ok(Map.of("available", available));
    }

    @Operation(summary = "Create a reservation")
    @PostMapping
    public ResponseEntity<Reservation> create(@RequestBody @Valid ReservationDto reservationDto) {
        return ResponseEntity.ok(reservationService.create(reservationDto));
    }

    @Operation(summary = "Update a reservation")
    @PutMapping("/{id}")
    public ResponseEntity<Reservation> update(@PathVariable UUID id, @RequestBody @Valid ReservationDto reservationDto) {
        return ResponseEntity.ok(reservationService.update(reservationDto, id));
    }

    @Operation(summary = "Delete / cancel a reservation")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        reservationService.delete(id);
        return ResponseEntity.ok().build();
    }
}
