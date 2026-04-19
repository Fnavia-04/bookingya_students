package com.project.bookingya.dtos;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoomDto {

    @Schema(description = "Unique room code")
    @NotBlank
    private String code;

    @Schema(description = "Commercial room name")
    @NotBlank
    private String name;

    @Schema(description = "City where the room is located")
    @NotBlank
    private String city;

    @Schema(description = "Maximum number of guests supported by the room")
    @NotNull
    @Min(1)
    private Integer maxGuests;

    @Schema(description = "Nightly price")
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal nightlyPrice;

    @Schema(description = "Whether the room can be reserved")
    @NotNull
    private Boolean available;
}
