package com.marcosmoreira.mitiendadebarrio.admin.shared.result;

import java.util.Optional;

/** Contrato local para operaciones del core embebido. */
public final class OperationResult<T> {

    private final boolean ok;
    private final T data;
    private final AppError error;
    private final String message;

    private OperationResult(boolean ok, T data, AppError error, String message) {
        this.ok = ok;
        this.data = data;
        this.error = error;
        this.message = message;
    }

    public static <T> OperationResult<T> success(T data) {
        return new OperationResult<>(true, data, null, "Operación completada.");
    }

    public static <T> OperationResult<T> success(T data, String message) {
        return new OperationResult<>(true, data, null, message);
    }

    public static <T> OperationResult<T> failure(AppError error) {
        return new OperationResult<>(false, null, error, error.message());
    }

    public static <T> OperationResult<T> failure(String message) {
        return new OperationResult<>(
                false,
                null,
                AppError.of(AppErrorCode.VALIDATION_ERROR, message),
                message
        );
    }

    public boolean ok() {
        return ok;
    }

    public boolean success() {
        return ok;
    }

    public Optional<T> data() {
        return Optional.ofNullable(data);
    }

    public Optional<AppError> error() {
        return Optional.ofNullable(error);
    }

    public String message() {
        return message;
    }
}
