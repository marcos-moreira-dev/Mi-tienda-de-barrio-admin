package com.marcosmoreira.mitiendadebarrio.admin.core.domain.catalogo;

/** Estado común para catálogos simples del sistema. */
public enum EstadoCatalogo {
    ACTIVA,
    INACTIVA;

    public static EstadoCatalogo fromDb(String value) {
        if (value == null || value.isBlank()) {
            return ACTIVA;
        }
        return EstadoCatalogo.valueOf(value.strip().toUpperCase());
    }

    public String dbValue() {
        return name();
    }

    public String label() {
        return this == ACTIVA ? "Activa" : "Inactiva";
    }
}
