package com.marcosmoreira.mitiendadebarrio.admin.core.domain.compra;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Línea de una compra avanzada. Permite registrar varios productos en una sola operación. */
public record DetalleCompraAvanzada(
        Long productoId,
        BigDecimal cantidad,
        BigDecimal costoUnitario,
        String codigoLote,
        LocalDate fechaVencimiento,
        String observacion
) {}
