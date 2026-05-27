package com.marcosmoreira.mitiendadebarrio.admin.core.domain.producto;

/** Estado comercial de un producto. */
public enum EstadoProducto {
    ACTIVO,
    INACTIVO;

    public static EstadoProducto fromDb(String value) {
        if (value == null || value.isBlank()) {
            return ACTIVO;
        }
        return EstadoProducto.valueOf(value.strip().toUpperCase());
    }

    public String dbValue() { return name(); }
    public String label() { return this == ACTIVO ? "Activo" : "Inactivo"; }
}
