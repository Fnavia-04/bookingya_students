package com.project.bookingya.models;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Data;

@Data
public class Room {
    private UUID id;
    private String code;
    private String name;
    private String city;
    private Integer maxGuests;
    private BigDecimal nightlyPrice;
    private Boolean available;
}
