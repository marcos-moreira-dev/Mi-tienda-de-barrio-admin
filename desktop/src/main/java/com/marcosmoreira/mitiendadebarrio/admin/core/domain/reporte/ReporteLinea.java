package com.marcosmoreira.mitiendadebarrio.admin.core.domain.reporte;

import java.util.List;

/** Línea genérica de reporte local, apta para UI y exportación CSV inicial. */
public record ReporteLinea(List<String> columnas) {
    public String asText() { return String.join(" · ", columnas); }
}
