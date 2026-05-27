package com.marcosmoreira.mitiendadebarrio.admin.core.domain.cartera;

import java.math.BigDecimal;

/** Resultado mínimo de una operación que conecta cartera/venta con caja. */
public record CarteraCajaResultado(
        String tipoOperacion,
        Long entidadId,
        Long movimientoCajaId,
        BigDecimal monto,
        BigDecimal saldoPendiente
) { }
