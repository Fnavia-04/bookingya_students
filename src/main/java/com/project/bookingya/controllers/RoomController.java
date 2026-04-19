package com.project.bookingya.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.project.bookingya.dtos.RoomDto;
import com.project.bookingya.models.Room;
import com.project.bookingya.services.RoomService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Rooms", description = "Room related operations")
@Validated
@RestController
@RequestMapping("/room")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @Operation(summary = "Get all rooms")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rooms retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<Room>> getAll() {
        return ResponseEntity.ok(roomService.getAll());
    }

    @Operation(summary = "Get a room by UUID")
    @GetMapping("/{id}")
    public ResponseEntity<Room> getById(@Parameter(description = "UUID of the room") @PathVariable UUID id) {
        return ResponseEntity.ok(roomService.getById(id));
    }

    @Operation(summary = "Get a room by code")
    @GetMapping("/code/{code}")
    public ResponseEntity<Room> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(roomService.getByCode(code));
    }

    @Operation(summary = "Create a room")
    @PostMapping
    public ResponseEntity<Room> create(@RequestBody @Valid RoomDto roomDto) {
        return ResponseEntity.ok(roomService.create(roomDto));
    }

    @Operation(summary = "Update a room")
    @PutMapping("/{id}")
    public ResponseEntity<Room> update(@PathVariable UUID id, @RequestBody @Valid RoomDto roomDto) {
        return ResponseEntity.ok(roomService.update(roomDto, id));
    }

    @Operation(summary = "Delete a room")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        roomService.delete(id);
        return ResponseEntity.ok().build();
    }
}
