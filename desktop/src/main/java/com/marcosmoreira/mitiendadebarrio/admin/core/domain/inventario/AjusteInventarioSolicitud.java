package com.marcosmoreira.mitiendadebarrio.admin.core.domain.inventario;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.movimiento.TipoMovimientoInventario;

import java.math.BigDecimal;

/** Solicitud local para registrar un ajuste formal de stock. */
public record AjusteInventarioSolicitud(
        Long productoId,
        TipoMovimientoInventario tipoMovimiento,
        BigDecimal cantidad,
        String motivo,
        String responsableTexto,
        String observacion,
        Long conteoInventarioId
) {
}
