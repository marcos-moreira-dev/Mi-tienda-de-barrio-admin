package com.marcosmoreira.mitiendadebarrio.admin.core.domain.inventario;

import java.time.LocalDateTime;

/** Cabecera de ajuste formal de inventario local. */
public record AjusteInventario(
        Long id,
        LocalDateTime fechaAjuste,
        Long conteoInventarioId,
        EstadoAjusteInventario estado,
        String responsableTexto,
        String motivo,
        String observacion
) {
}
