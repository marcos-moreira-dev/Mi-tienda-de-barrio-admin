package com.marcosmoreira.mitiendadebarrio.admin.core.domain.inventario;

/** Catálogo local de tipos de movimiento de inventario disponibles para reglas y reportes. */
public record TipoMovimientoInventarioCatalogo(
        String codigo,
        String nombre,
        int signo,
        boolean afectaStock,
        boolean reservadoSistema,
        String estado
) {
}
