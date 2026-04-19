package com.project.bookingya.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GuestDto {

    @Schema(description = "Guest document / identification")
    @NotBlank
    private String identification;

    @Schema(description = "Guest full name")
    @NotBlank
    private String name;

    @Schema(description = "Guest email")
    @NotBlank
    @Email
    private String email;
}
