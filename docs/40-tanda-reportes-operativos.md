# Tanda 7 — Reportes operativos

## Objetivo

Implementar reportes locales iniciales para operación diaria, reposición y revisión rápida del negocio.

## Alcance implementado

- Productos por comprar.
- Bajo stock.
- Agotados.
- Próximos a vencer.
- Inventario valorizado.
- Compras recientes.
- Ventas internas recientes.
- Mermas y retiros recientes.
- Exportación CSV local.

## Decisión de alcance

PDF básico fue agregado en la tanda 15. CSV sigue siendo el formato editable principal; el PDF actual es imprimible simple y puede pulirse después.

## Trazabilidad

JavaFX → ReporteOperativoService → ReporteOperativoRepository → SQLite → CSV en carpeta reports.
