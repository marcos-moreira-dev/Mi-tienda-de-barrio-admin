package com.marcosmoreira.mitiendadebarrio.admin.core.domain.respaldo;

import java.nio.file.Path;
import java.time.LocalDateTime;

/** Registro de un respaldo local de la base SQLite. */
public record RespaldoSistema(
        Long id,
        LocalDateTime fechaRespaldo,
        Path rutaArchivo,
        TipoRespaldo tipo,
        String estado,
        Long pesoBytes,
        String hashSha256,
        String observacion
) {
    public String resumen() {
        return fechaRespaldo + " · " + tipo + " · " + rutaArchivo.getFileName() + " · " + estado;
    }
}
