package com.project.bookingya.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.project.bookingya.dtos.GuestDto;
import com.project.bookingya.models.Guest;
import com.project.bookingya.services.GuestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Guests", description = "Guest related operations")
@Validated
@RestController
@RequestMapping("/guest")
public class GuestController {

    private final GuestService guestService;

    public GuestController(GuestService guestService) {
        this.guestService = guestService;
    }

    @Operation(summary = "Get all guests")
    @GetMapping
    public ResponseEntity<List<Guest>> getAll() {
        return ResponseEntity.ok(guestService.getAll());
    }

    @Operation(summary = "Get a guest by UUID")
    @GetMapping("/{id}")
    public ResponseEntity<Guest> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(guestService.getById(id));
    }

    @Operation(summary = "Get a guest by identification")
    @GetMapping("/identification/{identification}")
    public ResponseEntity<Guest> getByIdentification(@PathVariable String identification) {
        return ResponseEntity.ok(guestService.getByIdentification(identification));
    }

    @Operation(summary = "Create a guest")
    @PostMapping
    public ResponseEntity<Guest> create(@RequestBody @Valid GuestDto guestDto) {
        return ResponseEntity.ok(guestService.create(guestDto));
    }

    @Operation(summary = "Update a guest")
    @PutMapping("/{id}")
    public ResponseEntity<Guest> update(@PathVariable UUID id, @RequestBody @Valid GuestDto guestDto) {
        return ResponseEntity.ok(guestService.update(guestDto, id));
    }

    @Operation(summary = "Delete a guest")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        guestService.delete(id);
        return ResponseEntity.ok().build();
    }
}
