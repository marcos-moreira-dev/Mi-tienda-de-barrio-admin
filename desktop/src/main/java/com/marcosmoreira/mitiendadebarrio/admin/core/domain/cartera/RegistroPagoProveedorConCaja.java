package com.marcosmoreira.mitiendadebarrio.admin.core.domain.cartera;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.caja.MetodoPagoCaja;

import java.math.BigDecimal;

/** Comando para registrar un pago a proveedor y su egreso real en caja. */
public record RegistroPagoProveedorConCaja(
        Long cuentaPorPagarId,
        Long cajaDiariaId,
        BigDecimal monto,
        MetodoPagoCaja metodoPago,
        String referencia,
        String observacion
) { }
