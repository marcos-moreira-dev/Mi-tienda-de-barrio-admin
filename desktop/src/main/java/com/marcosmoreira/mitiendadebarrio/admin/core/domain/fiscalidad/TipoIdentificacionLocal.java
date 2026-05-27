package com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiscalidad;

/** Catálogo local de identificación para clientes/proveedores. */
public record TipoIdentificacionLocal(
        String codigo,
        String nombre,
        String descripcion,
        boolean activo
) {}
