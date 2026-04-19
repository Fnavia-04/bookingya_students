package com.project.bookingya.exceptions;

/**
 * Excepción lanzada cuando se intenta acceder a una entidad que no existe.
 * Ejemplo: obtener una reserva con un ID que no existe en la BD.
 */
public class EntityNotExistsException extends RuntimeException {
    public EntityNotExistsException(String message) {
        super(message);
    }

    public EntityNotExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
