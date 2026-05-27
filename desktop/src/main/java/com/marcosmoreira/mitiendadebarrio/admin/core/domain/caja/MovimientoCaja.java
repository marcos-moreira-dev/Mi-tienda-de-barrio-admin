package com.marcosmoreira.mitiendadebarrio.admin.core.domain.caja;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Movimiento manual de caja. */
public record MovimientoCaja(Long id, Long cajaDiariaId, TipoMovimientoCaja tipoMovimiento, String origen, Long referenciaId, BigDecimal monto, MetodoPagoCaja metodoPago, String descripcion, LocalDateTime fechaMovimiento) { }
