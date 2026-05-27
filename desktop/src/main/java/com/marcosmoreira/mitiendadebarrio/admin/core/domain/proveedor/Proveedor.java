package com.marcosmoreira.mitiendadebarrio.admin.core.domain.proveedor;

/** Proveedor o fuente de abastecimiento de la tienda. */
public record Proveedor(
        Long id,
        String nombre,
        String telefono,
        String whatsapp,
        String direccion,
        String observacion,
        EstadoProveedor estado
) {
    public static Proveedor nuevo(String nombre, String telefono, String whatsapp, String direccion, String observacion) {
        return new Proveedor(null, nombre, telefono, whatsapp, direccion, observacion, EstadoProveedor.ACTIVO);
    }

    public Proveedor conEstado(EstadoProveedor nuevoEstado) {
        return new Proveedor(id, nombre, telefono, whatsapp, direccion, observacion, nuevoEstado);
    }
}
