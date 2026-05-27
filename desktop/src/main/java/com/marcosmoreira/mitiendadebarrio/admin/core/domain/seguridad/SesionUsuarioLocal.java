package com.marcosmoreira.mitiendadebarrio.admin.core.domain.seguridad;

import java.util.List;

/** Resultado de un ingreso local correcto. */
public record SesionUsuarioLocal(
        Long usuarioId,
        String nombreUsuario,
        String nombreVisible,
        List<String> roles,
        boolean debeCambiarClave
) {
    public String nombreParaMostrar() {
        return nombreVisible == null || nombreVisible.isBlank() ? nombreUsuario : nombreVisible;
    }
}
