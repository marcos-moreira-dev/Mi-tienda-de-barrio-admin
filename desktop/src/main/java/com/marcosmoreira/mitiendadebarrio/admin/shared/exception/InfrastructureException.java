package com.marcosmoreira.mitiendadebarrio.admin.shared.exception;

/** Excepción local de la aplicación autocontenida. */
public class InfrastructureException extends RuntimeException {
    public InfrastructureException(String message) {
        super(message);
    }

    public InfrastructureException(String message, Throwable cause) {
        super(message, cause);
    }
}
