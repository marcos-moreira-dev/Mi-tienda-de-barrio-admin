# Tanda 20 — Ayuda operativa ampliada

## Objetivo

Convertir la ayuda contextual en un mini manual operativo dentro de la aplicación, sin depender de internet ni de documentación externa para las tareas diarias.

## Cambios realizados

- Se ampliaron las entradas de `ayuda_contextual` en el seed inicial.
- Se agregaron rutas operativas diaria y semanal.
- Se documentaron flujos críticos: venta pagada, venta fiada, compra pagada, compra a crédito, abonos, cierre de caja, gastos, respaldo, fiscalidad preparada y contabilidad básica.
- Se reforzó la advertencia de que los documentos preparados no reemplazan comprobantes autorizados por el SRI.
- Se amplió el contenido fallback de `SqliteAyudaContextualRepository` para instalaciones sin seed.

## Validación

Se agregó:

```text
scripts/validate-ayuda-operativa.bat
scripts/validate-ayuda-operativa.ps1
```

El test valida:

- entradas clave de ayuda operativa;
- ruta diaria;
- ruta semanal;
- ayuda de caja, fiado, cartera, inventario, reportes y respaldos;
- advertencia fiscal clara;
- ayuda de contabilidad básica;
- `integrity_check` y `foreign_key_check`.

## Decisión

La ayuda operativa queda como parte del producto, no como adorno. Debe servir para que el dueño o encargado recuerde qué hacer diariamente y semanalmente.
