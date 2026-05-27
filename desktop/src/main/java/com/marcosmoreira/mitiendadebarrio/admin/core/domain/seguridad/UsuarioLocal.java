package com.marcosmoreira.mitiendadebarrio.admin.core.domain.seguridad;

/** Cuenta local de usuario para operar la aplicación en esta computadora. */
public record UsuarioLocal(
        Long id,
        String nombreUsuario,
        String nombreVisible,
        EstadoUsuarioLocal estado,
        boolean debeCambiarClave,
        String ultimoAcceso
) { }
