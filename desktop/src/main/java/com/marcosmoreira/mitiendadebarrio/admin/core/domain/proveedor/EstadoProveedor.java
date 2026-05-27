package com.marcosmoreira.mitiendadebarrio.admin.core.domain.proveedor;

/** Estado operativo de un proveedor local. */
public enum EstadoProveedor {
    ACTIVO,
    INACTIVO;

    public static EstadoProveedor fromDb(String value) {
        if (value == null || value.isBlank()) {
            return ACTIVO;
        }
        return EstadoProveedor.valueOf(value.strip().toUpperCase());
    }

    public String dbValue() {
        return name();
    }

    public String label() {
        return this == ACTIVO ? "Activo" : "Inactivo";
    }
}
