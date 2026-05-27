package com.marcosmoreira.mitiendadebarrio.admin.bootstrap;

import java.util.List;

/** Resultado de verificación inicial de la aplicación. */
public record StartupReport(boolean ok, List<String> messages) {
}
