package com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad;

import java.math.BigDecimal;

/** Línea solicitada para registrar un asiento contable. */
public record AsientoContableDetalleSolicitud(
        Long cuentaId,
        String descripcion,
        BigDecimal debe,
        BigDecimal haber
) {}
