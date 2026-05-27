package com.marcosmoreira.mitiendadebarrio.admin.core.domain.seguridad;

/** Estado operativo de una cuenta local de usuario. */
public enum EstadoUsuarioLocal {
    ACTIVO("ACTIVO", "Activo"),
    INACTIVO("INACTIVO", "Inactivo"),
    BLOQUEADO("BLOQUEADO", "Bloqueado");

    private final String dbValue;
    private final String label;

    EstadoUsuarioLocal(String dbValue, String label) {
        this.dbValue = dbValue;
        this.label = label;
    }

    public String dbValue() {
        return dbValue;
    }

    public String label() {
        return label;
    }

    public static EstadoUsuarioLocal fromDb(String value) {
        if (value == null || value.isBlank()) {
            return ACTIVO;
        }
        for (EstadoUsuarioLocal estado : values()) {
            if (estado.dbValue.equalsIgnoreCase(value.strip())) {
                return estado;
            }
        }
        return ACTIVO;
    }
}
