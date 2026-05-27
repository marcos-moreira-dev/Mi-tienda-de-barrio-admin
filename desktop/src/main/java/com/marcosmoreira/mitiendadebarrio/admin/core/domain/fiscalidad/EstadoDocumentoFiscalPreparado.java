package com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiscalidad;

/** Estados locales para documentos fiscales preparados, sin autorización SRI real. */
public enum EstadoDocumentoFiscalPreparado {
    BORRADOR("BORRADOR"),
    PREPARADO("PREPARADO"),
    ANULADO("ANULADO");

    private final String dbValue;

    EstadoDocumentoFiscalPreparado(String dbValue) {
        this.dbValue = dbValue;
    }

    public String dbValue() {
        return dbValue;
    }

    public static EstadoDocumentoFiscalPreparado fromDb(String value) {
        if (value == null || value.isBlank()) {
            return PREPARADO;
        }
        for (EstadoDocumentoFiscalPreparado estado : values()) {
            if (estado.dbValue.equalsIgnoreCase(value.strip())) {
                return estado;
            }
        }
        return PREPARADO;
    }
}
