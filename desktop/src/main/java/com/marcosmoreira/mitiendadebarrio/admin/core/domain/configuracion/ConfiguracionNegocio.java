package com.marcosmoreira.mitiendadebarrio.admin.core.domain.configuracion;

/** Configuración visible del negocio dueño de la instalación local. */
public record ConfiguracionNegocio(
        String nombreComercial,
        String ruc,
        String responsable,
        String telefono,
        String direccion,
        String actividad,
        String moneda,
        String observacion
) {
    public static ConfiguracionNegocio vacia() {
        return new ConfiguracionNegocio(
                "Mi tienda de barrio",
                "", "", "", "",
                "Tienda / despensa de barrio",
                "USD", ""
        );
    }
}
