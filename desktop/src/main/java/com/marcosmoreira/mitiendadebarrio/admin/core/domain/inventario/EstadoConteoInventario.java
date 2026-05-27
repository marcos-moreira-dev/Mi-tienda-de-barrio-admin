package com.marcosmoreira.mitiendadebarrio.admin.core.domain.inventario;

/** Estados simples para un conteo físico local de inventario. */
public enum EstadoConteoInventario {
    ABIERTO,
    CERRADO,
    ANULADO;

    public static EstadoConteoInventario fromDb(String value) {
        if (value == null || value.isBlank()) {
            return ABIERTO;
        }
        try {
            return EstadoConteoInventario.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return ABIERTO;
        }
    }
}
