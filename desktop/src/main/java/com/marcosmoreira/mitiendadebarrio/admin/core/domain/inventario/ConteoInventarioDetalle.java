package com.marcosmoreira.mitiendadebarrio.admin.core.domain.inventario;

import java.math.BigDecimal;

/** Línea de conteo físico: compara stock del sistema contra cantidad contada. */
public record ConteoInventarioDetalle(
        Long id,
        Long conteoId,
        Long productoId,
        String productoNombre,
        BigDecimal stockSistema,
        BigDecimal stockContado,
        BigDecimal diferencia,
        String observacion
) {
}
