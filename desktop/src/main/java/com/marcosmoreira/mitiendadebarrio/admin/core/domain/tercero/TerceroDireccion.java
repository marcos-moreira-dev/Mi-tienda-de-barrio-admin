package com.marcosmoreira.mitiendadebarrio.admin.core.domain.tercero;

/** Dirección o referencia de ubicación de un tercero local. */
public record TerceroDireccion(
        Long id,
        long terceroId,
        String tipoDireccion,
        String direccion,
        String referencia,
        boolean principal,
        EstadoTercero estado
) {}
