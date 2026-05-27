package com.marcosmoreira.mitiendadebarrio.admin.core.application.reporte;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.reporte.ReporteOperativo;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.reporte.TipoReporteOperativo;
import com.marcosmoreira.mitiendadebarrio.admin.shared.result.OperationResult;

import java.nio.file.Path;

/** Casos de uso locales para reportes operativos. */
public final class ReporteOperativoService {
    private final ReporteOperativoRepository repository;
    private final ReporteExportService exportService;

    public ReporteOperativoService(ReporteOperativoRepository repository, ReporteExportService exportService) {
        this.repository = repository;
        this.exportService = exportService;
    }

    public ReporteOperativo generar(TipoReporteOperativo tipo) { return repository.generar(tipo); }
    public OperationResult<Path> exportarCsv(ReporteOperativo reporte) { return exportService.exportarCsv(reporte); }
    public OperationResult<Path> exportarPdfBasico(ReporteOperativo reporte) { return exportService.exportarPdfBasico(reporte); }
    public OperationResult<Path> exportarPdfFormal(ReporteOperativo reporte) { return exportService.exportarPdfFormal(reporte); }
}

