package com.marcosmoreira.mitiendadebarrio.admin.core.domain.cartera;

/** Comando para conectar una venta pagada con un ingreso real de caja. */
public record RegistroVentaPagadaEnCaja(
        Long ventaInternaId,
        Long cajaDiariaId,
        String observacion
) { }
