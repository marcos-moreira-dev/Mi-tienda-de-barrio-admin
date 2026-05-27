package com.marcosmoreira.mitiendadebarrio.admin.core.domain.opcional;

import java.math.BigDecimal;

public record RegistroActivoNegocio(
        Long tipoActivoId,
        String codigo,
        String nombre,
        String descripcion,
        String fechaAdquisicion,
        BigDecimal valorEstimado,
        String ubicacion,
        String responsable,
        String observacion
) {}
