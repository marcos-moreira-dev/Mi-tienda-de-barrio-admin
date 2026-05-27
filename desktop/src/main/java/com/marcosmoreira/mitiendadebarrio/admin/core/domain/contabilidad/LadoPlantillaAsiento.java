package com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad;

/** Lado contable usado por una plantilla simple de asiento. */
public enum LadoPlantillaAsiento {
    DEBE,
    HABER;

    public static LadoPlantillaAsiento fromDb(String value) {
        if (value == null || value.isBlank()) {
            return DEBE;
        }
        return LadoPlantillaAsiento.valueOf(value.strip().toUpperCase());
    }
}
