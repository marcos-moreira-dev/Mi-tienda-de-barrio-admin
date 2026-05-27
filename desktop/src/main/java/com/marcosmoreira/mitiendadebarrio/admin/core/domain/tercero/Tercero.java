package com.marcosmoreira.mitiendadebarrio.admin.core.domain.tercero;

/**
 * Persona, negocio o entidad relacionada con la tienda.
 * Puede actuar como cliente, proveedor o ambas cosas mediante perfiles.
 */
public record Tercero(
        Long id,
        TipoTercero tipo,
        String tipoIdentificacion,
        String numeroIdentificacion,
        String nombreLegal,
        String nombreComercial,
        String telefono,
        String whatsapp,
        String correo,
        String observacion,
        EstadoTercero estado,
        boolean cliente,
        boolean proveedor
) {
    public static Tercero nuevo(
            TipoTercero tipo,
            String tipoIdentificacion,
            String numeroIdentificacion,
            String nombreLegal,
            String nombreComercial,
            String telefono,
            String whatsapp,
            String correo,
            String observacion
    ) {
        return new Tercero(
                null,
                tipo == null ? TipoTercero.PERSONA_NATURAL : tipo,
                tipoIdentificacion,
                numeroIdentificacion,
                nombreLegal,
                nombreComercial,
                telefono,
                whatsapp,
                correo,
                observacion,
                EstadoTercero.ACTIVO,
                false,
                false
        );
    }

    public String nombreVisible() {
        if (nombreComercial != null && !nombreComercial.isBlank()) {
            return nombreComercial.strip();
        }
        return nombreLegal == null ? "" : nombreLegal.strip();
    }

    public Tercero conEstado(EstadoTercero nuevoEstado) {
        return new Tercero(
                id, tipo, tipoIdentificacion, numeroIdentificacion, nombreLegal, nombreComercial,
                telefono, whatsapp, correo, observacion, nuevoEstado, cliente, proveedor
        );
    }

    public Tercero conPerfiles(boolean cliente, boolean proveedor) {
        return new Tercero(
                id, tipo, tipoIdentificacion, numeroIdentificacion, nombreLegal, nombreComercial,
                telefono, whatsapp, correo, observacion, estado, cliente, proveedor
        );
    }
}
