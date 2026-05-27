package com.marcosmoreira.mitiendadebarrio.admin.shared.exception;

/** Excepción local de la aplicación autocontenida. */
public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
