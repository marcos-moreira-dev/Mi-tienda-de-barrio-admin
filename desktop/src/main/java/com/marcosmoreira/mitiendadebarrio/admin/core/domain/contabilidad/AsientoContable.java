package com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad;

import java.math.BigDecimal;
import java.util.List;

/** Asiento contable básico local. No reemplaza contabilidad profesional completa. */
public record AsientoContable(
        Long id,
        String numeroAsiento,
        String tipoDiarioCodigo,
        String fechaAsiento,
        int periodoAnio,
        int periodoMes,
        String concepto,
        EstadoAsientoContable estado,
        String origenTipo,
        Long origenId,
        BigDecimal totalDebe,
        BigDecimal totalHaber,
        List<AsientoContableDetalle> detalles
) {}
