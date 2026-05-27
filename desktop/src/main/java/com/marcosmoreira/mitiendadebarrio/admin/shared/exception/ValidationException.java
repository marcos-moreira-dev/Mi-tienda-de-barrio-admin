package com.marcosmoreira.mitiendadebarrio.admin.shared.exception;

/** Excepción local de la aplicación autocontenida. */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
