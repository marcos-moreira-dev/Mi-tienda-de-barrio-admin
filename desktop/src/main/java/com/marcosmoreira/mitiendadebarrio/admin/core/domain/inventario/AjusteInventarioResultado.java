package com.marcosmoreira.mitiendadebarrio.admin.core.domain.inventario;

import java.math.BigDecimal;

/** Resultado de un ajuste que actualizó stock y dejó movimiento trazable. */
public record AjusteInventarioResultado(
        Long ajusteInventarioId,
        Long movimientoInventarioId,
        Long productoId,
        BigDecimal stockAnterior,
        BigDecimal stockNuevo
) {
}
