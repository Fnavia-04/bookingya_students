package com.project.bookingya.dtos;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationDto {
    private UUID roomId;
    private UUID guestId;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private Integer guestsCount;
}
