package com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad;

/** Diario contable simple para clasificar asientos locales. */
public record TipoDiarioContable(
        String codigo,
        String nombre,
        boolean activo
) {}
