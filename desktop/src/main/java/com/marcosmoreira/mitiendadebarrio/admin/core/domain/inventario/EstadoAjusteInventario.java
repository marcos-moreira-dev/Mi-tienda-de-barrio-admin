package com.marcosmoreira.mitiendadebarrio.admin.core.domain.inventario;

/** Estados simples para ajustes de inventario. */
public enum EstadoAjusteInventario {
    REGISTRADO,
    ANULADO;

    public static EstadoAjusteInventario fromDb(String value) {
        if (value == null || value.isBlank()) {
            return REGISTRADO;
        }
        try {
            return EstadoAjusteInventario.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return REGISTRADO;
        }
    }
}
