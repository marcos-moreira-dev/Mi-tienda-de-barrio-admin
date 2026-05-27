package com.marcosmoreira.mitiendadebarrio.admin.core.domain.tercero;

/** Tipo general de tercero local. */
public enum TipoTercero {
    PERSONA_NATURAL,
    NEGOCIO,
    CONSUMIDOR_FINAL,
    OTRO;

    public static TipoTercero fromDb(String value) {
        if (value == null || value.isBlank()) {
            return PERSONA_NATURAL;
        }
        return TipoTercero.valueOf(value.strip().toUpperCase());
    }

    public String dbValue() {
        return name();
    }

    public String label() {
        return switch (this) {
            case PERSONA_NATURAL -> "Persona natural";
            case NEGOCIO -> "Negocio";
            case CONSUMIDOR_FINAL -> "Consumidor final";
            case OTRO -> "Otro";
        };
    }
}
