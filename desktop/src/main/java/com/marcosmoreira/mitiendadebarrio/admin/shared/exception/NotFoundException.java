package com.marcosmoreira.mitiendadebarrio.admin.shared.exception;

/** Excepción local de la aplicación autocontenida. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
