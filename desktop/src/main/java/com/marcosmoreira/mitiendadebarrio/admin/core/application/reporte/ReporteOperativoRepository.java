package com.marcosmoreira.mitiendadebarrio.admin.core.application.reporte;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.reporte.ReporteOperativo;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.reporte.TipoReporteOperativo;

/** Puerto de consulta para reportes operativos locales. */
public interface ReporteOperativoRepository {
    ReporteOperativo generar(TipoReporteOperativo tipo);
}
