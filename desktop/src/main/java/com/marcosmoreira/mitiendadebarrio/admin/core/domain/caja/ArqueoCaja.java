package com.marcosmoreira.mitiendadebarrio.admin.core.domain.caja;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Arqueo puntual de caja local. */
public record ArqueoCaja(
        Long id,
        Long cajaDiariaId,
        LocalDateTime fechaArqueo,
        BigDecimal saldoSistema,
        BigDecimal saldoContado,
        BigDecimal diferencia,
        String responsableTexto,
        String observacion
) { }
