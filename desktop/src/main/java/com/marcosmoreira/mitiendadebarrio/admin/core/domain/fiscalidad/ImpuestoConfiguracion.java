package com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiscalidad;

import java.math.BigDecimal;

/** Impuesto configurable local. El porcentaje debe revisarse antes de operar en un negocio real. */
public record ImpuestoConfiguracion(
        Long id,
        String codigo,
        String nombre,
        BigDecimal porcentaje,
        String fechaInicio,
        String fechaFin,
        boolean aplicaVentas,
        boolean aplicaCompras,
        boolean activo,
        String observacion
) {}
