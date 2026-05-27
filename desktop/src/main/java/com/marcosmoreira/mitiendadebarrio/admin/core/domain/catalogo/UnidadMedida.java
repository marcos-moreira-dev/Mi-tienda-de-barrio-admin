package com.marcosmoreira.mitiendadebarrio.admin.core.domain.catalogo;

/** Unidad de medida usada para expresar stock, compras y ventas internas. */
public record UnidadMedida(
        Long id,
        String nombre,
        String abreviatura,
        boolean permiteDecimales,
        EstadoCatalogo estado
) {
    public static UnidadMedida nueva(String nombre, String abreviatura, boolean permiteDecimales) {
        return new UnidadMedida(null, nombre, abreviatura, permiteDecimales, EstadoCatalogo.ACTIVA);
    }

    public UnidadMedida conEstado(EstadoCatalogo nuevoEstado) {
        return new UnidadMedida(id, nombre, abreviatura, permiteDecimales, nuevoEstado);
    }

    public String permiteDecimalesTexto() {
        return permiteDecimales ? "Sí" : "No";
    }
}
