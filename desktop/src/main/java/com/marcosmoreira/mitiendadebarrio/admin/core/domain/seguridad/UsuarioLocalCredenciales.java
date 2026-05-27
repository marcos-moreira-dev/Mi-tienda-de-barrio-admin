package com.marcosmoreira.mitiendadebarrio.admin.core.domain.seguridad;

/** Datos internos necesarios para verificar una contraseña local. */
public record UsuarioLocalCredenciales(
        Long id,
        String nombreUsuario,
        String nombreVisible,
        String passwordHash,
        String passwordSalt,
        String algoritmoHash,
        EstadoUsuarioLocal estado,
        boolean debeCambiarClave
) { }
