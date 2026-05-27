package com.marcosmoreira.mitiendadebarrio.admin.core.domain.venta;

import java.math.BigDecimal;

/** Comando simple para registrar una salida por venta interna de un producto. */
public record RegistroVentaInternaSimple(
        Long productoId,
        BigDecimal cantidad,
        BigDecimal precioUnitario,
        MetodoPagoVentaInterna metodoPago,
        String numeroReferencia,
        boolean advertenciaTributariaAceptada,
        String observacion
) {}
