package com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad;

import java.math.BigDecimal;

/** Solicitud para generar un asiento contable desde una plantilla simple. */
public record CrearAsientoDesdePlantillaSolicitud(
        String plantillaCodigo,
        String eventoCodigo,
        String fechaAsiento,
        String concepto,
        String origenTipo,
        Long origenId,
        BigDecimal importe
) {}
