package com.marcosmoreira.mitiendadebarrio.admin.core.domain.tercero;

/** Estado operativo de una persona, cliente o proveedor registrado como tercero local. */
public enum EstadoTercero {
    ACTIVO,
    INACTIVO;

    public static EstadoTercero fromDb(String value) {
        if (value == null || value.isBlank()) {
            return ACTIVO;
        }
        return EstadoTercero.valueOf(value.strip().toUpperCase());
    }

    public String dbValue() {
        return name();
    }

    public String label() {
        return this == ACTIVO ? "Activo" : "Inactivo";
    }
}
