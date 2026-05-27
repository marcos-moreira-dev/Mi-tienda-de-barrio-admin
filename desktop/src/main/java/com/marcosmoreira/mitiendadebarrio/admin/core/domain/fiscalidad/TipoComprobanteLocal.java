package com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiscalidad;

/** Tipo local de documento preparado. No representa autorización tributaria real. */
public record TipoComprobanteLocal(
        String codigo,
        String nombre,
        String descripcion,
        boolean requiereTercero,
        String advertenciaNoAutorizado,
        boolean activo
) {}
