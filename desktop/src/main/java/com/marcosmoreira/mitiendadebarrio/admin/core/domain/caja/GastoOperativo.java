package com.marcosmoreira.mitiendadebarrio.admin.core.domain.caja;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Gasto operativo diario conectado a caja. */
public record GastoOperativo(
        Long id,
        Long cajaDiariaId,
        Long tipoGastoId,
        Long movimientoCajaId,
        LocalDateTime fechaGasto,
        BigDecimal monto,
        MetodoPagoCaja formaPago,
        String descripcion,
        String referencia,
        String observacion
) { }
