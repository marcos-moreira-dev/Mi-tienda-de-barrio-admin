package com.marcosmoreira.mitiendadebarrio.admin.shared.result;

/** Error entendible para capas superiores de la aplicación. */
public record AppError(AppErrorCode code, String message, String detail) {
    public static AppError of(AppErrorCode code, String message) {
        return new AppError(code, message, null);
    }
}
