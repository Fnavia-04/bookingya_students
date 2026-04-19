package com.project.bookingya.models;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {
    private UUID id;
    private UUID roomId;
    private UUID guestId;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private Integer guestsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
