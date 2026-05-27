package com.marcosmoreira.mitiendadebarrio.admin.core.domain.opcional;

import java.math.BigDecimal;

/** Bien operativo del negocio; no equivale a control contable formal de activos fijos. */
public record ActivoNegocio(
        Long id,
        Long tipoActivoId,
        String codigo,
        String nombre,
        String descripcion,
        String fechaAdquisicion,
        BigDecimal valorEstimado,
        String estado,
        String ubicacion,
        String responsable,
        String observacion
) {}
