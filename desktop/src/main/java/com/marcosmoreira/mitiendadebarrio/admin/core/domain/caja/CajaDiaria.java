package com.marcosmoreira.mitiendadebarrio.admin.core.domain.caja;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Caja operativa diaria, local y no contable formal. */
public record CajaDiaria(Long id, LocalDate fecha, BigDecimal saldoInicial, BigDecimal totalIngresos, BigDecimal totalEgresos, BigDecimal saldoEsperado, BigDecimal saldoContado, BigDecimal diferencia, EstadoCajaDiaria estado, String observacion) { }
