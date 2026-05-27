package com.marcosmoreira.mitiendadebarrio.admin.core.domain.venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Venta interna operativa; no reemplaza comprobantes oficiales. */
public record VentaInterna(
        Long id,
        LocalDateTime fechaVenta,
        BigDecimal total,
        MetodoPagoVentaInterna metodoPago,
        String numeroReferencia,
        String estado,
        boolean advertenciaTributariaAceptada,
        String observacion
) {}
