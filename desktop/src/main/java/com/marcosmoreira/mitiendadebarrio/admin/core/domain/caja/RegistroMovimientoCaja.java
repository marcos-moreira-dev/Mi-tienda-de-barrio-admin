package com.marcosmoreira.mitiendadebarrio.admin.core.domain.caja;

import java.math.BigDecimal;

/** Comando simple para registrar ingreso/egreso manual de caja. */
public record RegistroMovimientoCaja(Long cajaDiariaId, TipoMovimientoCaja tipoMovimiento, BigDecimal monto, MetodoPagoCaja metodoPago, String descripcion) { }
