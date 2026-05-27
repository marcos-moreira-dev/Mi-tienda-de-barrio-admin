package com.marcosmoreira.mitiendadebarrio.admin.core.domain.venta;

import java.math.BigDecimal;

/** Detalle de una venta interna con varios productos. */
public record DetalleVentaInternaAvanzada(
        Long productoId,
        BigDecimal cantidad,
        BigDecimal precioUnitario,
        String observacion
) {}
