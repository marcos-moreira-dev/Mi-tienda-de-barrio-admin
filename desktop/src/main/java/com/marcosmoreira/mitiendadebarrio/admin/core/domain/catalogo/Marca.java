package com.marcosmoreira.mitiendadebarrio.admin.core.domain.catalogo;

/** Marca opcional de producto, útil para filtrar, ordenar y evitar nombres duplicados. */
public record Marca(
        Long id,
        String nombre,
        String descripcion,
        EstadoCatalogo estado
) {
    public static Marca nueva(String nombre, String descripcion) {
        return new Marca(null, nombre, descripcion, EstadoCatalogo.ACTIVA);
    }

    public Marca conEstado(EstadoCatalogo nuevoEstado) {
        return new Marca(id, nombre, descripcion, nuevoEstado);
    }
}
