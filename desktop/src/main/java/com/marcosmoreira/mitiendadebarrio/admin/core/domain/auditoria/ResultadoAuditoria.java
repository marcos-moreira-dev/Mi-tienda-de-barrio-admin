package com.marcosmoreira.mitiendadebarrio.admin.core.domain.auditoria;

/** Resultado operacional registrado en la bitácora local. */
public enum ResultadoAuditoria {
    OK,
    ADVERTENCIA,
    ERROR;

    public static ResultadoAuditoria fromDb(String value) {
        if (value == null || value.isBlank()) {
            return ADVERTENCIA;
        }
        return ResultadoAuditoria.valueOf(value.strip().toUpperCase());
    }
}
