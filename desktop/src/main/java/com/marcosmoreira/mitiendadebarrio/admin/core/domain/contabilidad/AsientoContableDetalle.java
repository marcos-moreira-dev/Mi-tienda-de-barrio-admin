package com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad;

import java.math.BigDecimal;

/** Línea de asiento contable local. */
public record AsientoContableDetalle(
        Long id,
        Long asientoId,
        Long cuentaId,
        int linea,
        String descripcion,
        BigDecimal debe,
        BigDecimal haber
) {}
