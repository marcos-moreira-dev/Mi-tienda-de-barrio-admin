package com.marcosmoreira.mitiendadebarrio.admin.core.domain.cartera;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.caja.MetodoPagoCaja;

import java.math.BigDecimal;

/** Comando para registrar un abono de fiado y su ingreso real en caja. */
public record RegistroAbonoConCaja(
        Long cuentaPorCobrarId,
        Long cajaDiariaId,
        BigDecimal monto,
        MetodoPagoCaja metodoPago,
        String observacion
) { }
