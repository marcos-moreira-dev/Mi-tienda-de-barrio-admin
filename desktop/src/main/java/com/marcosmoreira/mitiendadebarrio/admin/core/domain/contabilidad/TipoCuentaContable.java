package com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad;

/** Tipo contable mínimo para plan de cuentas local. */
public enum TipoCuentaContable {
    ACTIVO,
    PASIVO,
    PATRIMONIO,
    INGRESO,
    GASTO,
    COSTO;

    public static TipoCuentaContable fromDb(String value) {
        if (value == null || value.isBlank()) {
            return ACTIVO;
        }
        return TipoCuentaContable.valueOf(value.strip().toUpperCase());
    }
}
