package com.marcosmoreira.mitiendadebarrio.admin.core.application.seguridad;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.seguridad.UsuarioLocalCredenciales;

import java.util.List;
import java.util.Optional;

/** Puerto de acceso a usuarios, roles y permisos locales. */
public interface UsuarioLocalRepository {
    Optional<UsuarioLocalCredenciales> buscarCredencialesPorUsuario(String nombreUsuario);
    List<String> rolesDeUsuario(long usuarioId);
    void actualizarUltimoAcceso(long usuarioId);
}
