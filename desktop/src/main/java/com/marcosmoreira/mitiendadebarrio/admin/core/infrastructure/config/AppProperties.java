package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.config;

/** Configuración mínima de aplicación. */
public record AppProperties(String appName, String version, String environment) {
    public static AppProperties loadDefault() {
        return new AppProperties("Mi tienda de barrio admin", "0.1.0-SNAPSHOT", "local");
    }
}
