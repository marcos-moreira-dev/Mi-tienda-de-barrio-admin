package com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiscalidad;

import java.math.BigDecimal;

/** Línea de documento fiscal preparado. */
public record DocumentoFiscalPreparadoDetalle(
        Long id,
        Long documentoId,
        Long productoId,
        String descripcion,
        BigDecimal cantidad,
        BigDecimal precioUnitario,
        BigDecimal baseImponible,
        Long impuestoId,
        BigDecimal valorImpuesto,
        BigDecimal totalLinea
) {}
