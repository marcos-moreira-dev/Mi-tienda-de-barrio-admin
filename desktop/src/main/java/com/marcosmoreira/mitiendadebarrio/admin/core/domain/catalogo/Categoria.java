package com.marcosmoreira.mitiendadebarrio.admin.core.domain.catalogo;

/** Categoría comercial para agrupar productos en reportes, búsqueda y reposición. */
public record Categoria(
        Long id,
        String nombre,
        String descripcion,
        EstadoCatalogo estado
) {
    public static Categoria nueva(String nombre, String descripcion) {
        return new Categoria(null, nombre, descripcion, EstadoCatalogo.ACTIVA);
    }

    public Categoria conEstado(EstadoCatalogo nuevoEstado) {
        return new Categoria(id, nombre, descripcion, nuevoEstado);
    }
}
