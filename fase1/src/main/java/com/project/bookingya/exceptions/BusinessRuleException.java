package com.project.bookingya.exceptions;

/**
 * Excepción lanzada cuando se viola una regla de negocio.
 * Ejemplo: intentar crear una reserva cuando hay solapamiento de fechas.
 */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }

    public BusinessRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
