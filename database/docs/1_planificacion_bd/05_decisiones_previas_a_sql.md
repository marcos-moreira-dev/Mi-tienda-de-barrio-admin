# 05. Decisiones previas a SQL

## Propósito

Congelar decisiones antes de escribir el SQL V001 oficial.

## Decisiones tomadas

1. SQLite es la base oficial V001.
2. `producto` es el núcleo del dominio.
3. `movimiento_inventario` es la historia funcional del stock.
4. Compras y ventas internas se separan.
5. `venta_interna` es control operativo, no facturación.
6. Caja y fiado quedan físicamente listos, pero pueden ocultarse por configuración.
7. Lote y vencimiento existen desde V001 como capacidad opcional.
8. Estados estables se controlan con `CHECK`.
9. Categoría, marca, unidad y proveedor sí son tablas.
10. Fotos y evidencias se guardan como rutas locales, no como blobs.
11. La licencia puede limitar operaciones nuevas, pero no bloquear datos.

## Tipos

- Fechas: texto ISO-8601 (`TEXT` con `datetime('now')`).
- Montos: `NUMERIC` con checks.
- Booleanos: `INTEGER` con `CHECK (campo IN (0,1))`.
- Estados: `TEXT` con `CHECK`.
- IDs: `INTEGER PRIMARY KEY AUTOINCREMENT`.

## Triggers

Se permiten triggers mínimos para `updated_at`. La lógica de negocio pertenece a la capa de aplicación.
