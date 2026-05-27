package com.marcosmoreira.mitiendadebarrio.admin.core.domain.caja;

import java.math.BigDecimal;

/** Comando local para registrar gasto operativo y su egreso de caja. */
public record RegistroGastoOperativo(
        Long cajaDiariaId,
        Long tipoGastoId,
        BigDecimal monto,
        MetodoPagoCaja formaPago,
        String descripcion,
        String referencia,
        String observacion
) { }
