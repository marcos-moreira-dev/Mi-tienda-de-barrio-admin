# Tanda 15 — Reportes y PDF formal

## Objetivo

Fortalecer los reportes operativos y reemplazar la salida PDF básica por una salida local más formal, imprimible y útil para la operación diaria de una tienda.

Esta tanda no implementa fiscalidad ni SRI real. Los reportes siguen siendo internos y operativos.

## Cambios principales

Se agregaron nuevos tipos de reporte:

- Cierres de caja recientes.
- Fiado pendiente.
- Abonos recientes.
- Gastos operativos.
- Cuentas por pagar.

Se mejoró el exportador PDF:

- encabezado formal;
- nombre del reporte;
- fecha de generación;
- cantidad de registros;
- resumen operativo;
- detalle tabular;
- paginación simple;
- pie de página;
- compatibilidad sin dependencias externas.

El método anterior `exportarPdfBasico` se conserva por compatibilidad, pero ahora delega a la generación formal.

## Archivos modificados

- `TipoReporteOperativo.java`
- `SqliteReporteOperativoRepository.java`
- `ReporteExportService.java`
- `ReporteOperativoService.java`
- `ReportesOperativosView.java`

## Scripts agregados

- `scripts/validate-reportes-pdf-formal.bat`
- `scripts/validate-reportes-pdf-formal.ps1`

## Validaciones

El script valida:

- existencia de los nuevos reportes;
- conexión del repositorio SQLite;
- método `exportarPdfFormal`;
- uso del PDF formal desde la vista;
- consultas SQL de caja, fiado, abonos, gastos y cuentas por pagar;
- `foreign_key_check` correcto.

## Decisión

El PDF formal queda como salida local imprimible. CSV sigue siendo el formato editable principal.

Reportes y PDF formal quedan listos como base para las siguientes tandas: fiscalidad preparada, contabilidad y ayuda operativa ampliada.
