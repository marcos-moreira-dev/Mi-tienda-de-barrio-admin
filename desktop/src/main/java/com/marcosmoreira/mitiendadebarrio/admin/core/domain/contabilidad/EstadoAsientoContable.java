package com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad;

/** Estado mínimo del asiento contable local. */
public enum EstadoAsientoContable {
    BORRADOR,
    REGISTRADO,
    ANULADO,
    REVERSADO;

    public static EstadoAsientoContable fromDb(String value) {
        if (value == null || value.isBlank()) {
            return BORRADOR;
        }
        return EstadoAsientoContable.valueOf(value.strip().toUpperCase());
    }
}
