package com.marcosmoreira.mitiendadebarrio.admin.core.domain.contabilidad;

/** Línea de una plantilla de asiento contable local. */
public record PlantillaAsientoDetalle(
        Long id,
        Long plantillaId,
        Long cuentaId,
        int linea,
        LadoPlantillaAsiento lado,
        String descripcion
) {}
