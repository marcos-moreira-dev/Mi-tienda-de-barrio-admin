package com.marcosmoreira.mitiendadebarrio.admin.core.domain.venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Pago registrado para una venta interna pagada. */
public record VentaPago(
        Long id,
        Long ventaInternaId,
        LocalDateTime fechaPago,
        BigDecimal monto,
        MetodoPagoVentaInterna metodoPago,
        String referencia,
        String observacion
) {}
