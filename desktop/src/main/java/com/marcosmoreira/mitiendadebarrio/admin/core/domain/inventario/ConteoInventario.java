package com.marcosmoreira.mitiendadebarrio.admin.core.domain.inventario;

import java.time.LocalDateTime;

/** Cabecera de conteo físico de inventario. */
public record ConteoInventario(
        Long id,
        LocalDateTime fechaConteo,
        EstadoConteoInventario estado,
        String responsableTexto,
        String observacion
) {
}
