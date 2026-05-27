package com.marcosmoreira.mitiendadebarrio.admin.core.domain.reporte;

import java.time.LocalDateTime;
import java.util.List;

/** Resultado de un reporte operativo generado localmente. */
public record ReporteOperativo(
        TipoReporteOperativo tipo,
        List<String> encabezados,
        List<ReporteLinea> lineas,
        LocalDateTime generadoEn
) {
    public boolean vacio() { return lineas == null || lineas.isEmpty(); }
}
