package com.project.bookingya.models;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class Reservation {
    private UUID id;
    private UUID guestId;
    private UUID roomId;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private Integer guestsCount;
    private String notes;
}
