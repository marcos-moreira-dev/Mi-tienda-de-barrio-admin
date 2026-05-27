package com.marcosmoreira.mitiendadebarrio.admin.core.domain.movimiento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Movimiento trazable de inventario para un producto. */
public record MovimientoInventario(
        Long id,
        Long productoId,
        String productoNombre,
        Long loteId,
        TipoMovimientoInventario tipoMovimiento,
        BigDecimal cantidad,
        BigDecimal stockAnterior,
        BigDecimal stockNuevo,
        LocalDateTime fechaMovimiento,
        String referenciaTipo,
        Long referenciaId,
        String motivo,
        String responsableTexto,
        String observacion
) {
}
