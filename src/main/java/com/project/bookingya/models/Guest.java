package com.project.bookingya.models;

import java.util.UUID;

import lombok.Data;

@Data
public class Guest {
    private UUID id;
    private String identification;
    private String name;
    private String email;
}
