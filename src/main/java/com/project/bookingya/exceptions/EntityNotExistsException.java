package com.project.bookingya.exceptions;

public class EntityNotExistsException extends RuntimeException {
    public EntityNotExistsException(String message) {
        super(message);
    }
}
