package com.marcosmoreira.mitiendadebarrio.admin.shared.exception;

/** Excepción local de la aplicación autocontenida. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
