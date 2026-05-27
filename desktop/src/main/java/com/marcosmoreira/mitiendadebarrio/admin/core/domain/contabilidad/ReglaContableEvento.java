package com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad;

/** Regla que asocia un evento operativo con una plantilla contable sugerida. */
public record ReglaContableEvento(
        Long id,
        String eventoCodigo,
        Long plantillaId,
        String descripcion,
        boolean activo
) {}
