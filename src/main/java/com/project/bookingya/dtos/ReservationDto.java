package com.project.bookingya.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReservationDto {

    @Schema(description = "Guest UUID")
    @NotNull
    private UUID guestId;

    @Schema(description = "Room UUID")
    @NotNull
    private UUID roomId;

    @Schema(description = "Reservation check-in")
    @NotNull
    private LocalDateTime checkIn;

    @Schema(description = "Reservation check-out")
    @NotNull
    private LocalDateTime checkOut;

    @Schema(description = "Amount of guests included in the reservation")
    @NotNull
    @Min(1)
    private Integer guestsCount;

    @Schema(description = "Additional notes")
    private String notes;
}
