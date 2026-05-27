package com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad;

import java.util.List;

/** Plantilla mínima para sugerir asientos contables repetitivos. */
public record PlantillaAsiento(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        boolean activo,
        List<PlantillaAsientoDetalle> detalles
) {}
