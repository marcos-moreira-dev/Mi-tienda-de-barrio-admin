package com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad;

import java.util.List;

/** Solicitud para registrar un asiento contable local. */
public record CrearAsientoContableSolicitud(
        String tipoDiarioCodigo,
        String fechaAsiento,
        String concepto,
        String origenTipo,
        Long origenId,
        List<AsientoContableDetalleSolicitud> detalles
) {}
