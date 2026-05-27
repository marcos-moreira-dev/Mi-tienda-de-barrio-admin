package com.marcosmoreira.mitiendadebarrio.admin.core.domain.tercero;

/** Contacto adicional de un tercero local. */
public record TerceroContacto(
        Long id,
        long terceroId,
        String nombre,
        String cargo,
        String telefono,
        String whatsapp,
        String correo,
        boolean principal,
        String observacion,
        EstadoTercero estado
) {}
