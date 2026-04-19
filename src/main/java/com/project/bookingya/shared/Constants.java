package com.project.bookingya.shared;

public final class Constants {
    private Constants() {
    }

    public static final String ERROR = "error";
    public static final String BODY_INVALID = "Body Invalid";

    public static final String ROOM_NOT_FOUND = "Room not found";
    public static final String ROOM_EXISTS = "Room already exists";
    public static final String ROOM_NOT_AVAILABLE = "Room is not available";

    public static final String GUEST_NOT_FOUND = "Guest not found";
    public static final String GUEST_EXISTS = "Guest already exists";
    public static final String GUEST_EMAIL_EXISTS = "Guest email already exists";

    public static final String RESERVATION_NOT_FOUND = "Reservation not found";
    public static final String RESERVATION_OVERLAP_ROOM = "The room already has a reservation in that time range";
    public static final String RESERVATION_OVERLAP_GUEST = "The guest already has a reservation in that time range";
    public static final String INVALID_RESERVATION_RANGE = "checkIn must be before checkOut";
    public static final String INVALID_GUESTS_COUNT = "guestsCount must be greater than zero";
    public static final String ROOM_CAPACITY_EXCEEDED = "guestsCount exceeds room capacity";
}
