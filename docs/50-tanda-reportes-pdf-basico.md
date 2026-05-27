# Tanda 15 — Reportes PDF básicos

## Objetivo

Agregar exportación PDF local básica sin convertir el sistema en una solución pesada ni depender todavía de librerías externas de reporting.

## Implementado

- `ReporteExportService.exportarPdfBasico`.
- `ReporteOperativoService.exportarPdfBasico`.
- Botón `Exportar PDF` en `ReportesOperativosView`.

## Decisión técnica

Se agregó un generador PDF mínimo basado en texto, suficiente para imprimir reportes simples. CSV sigue siendo el formato editable principal.

## Límites actuales

- Una sola página.
- Truncado de líneas largas.
- Sin tablas visuales avanzadas.
- Sin logos ni encabezados institucionales elaborados.

## Pulido posterior

Una tanda futura puede reemplazar el PDF básico por:

- OpenPDF/iText compatible con licencias revisadas.
- Apache PDFBox.
- JasperReports, si se justifica.
- Plantillas imprimibles más profesionales.

