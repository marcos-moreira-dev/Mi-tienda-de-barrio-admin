# UI — Movimientos, compras y salidas

## Objetivo

Agregar tres módulos operativos usando la carcasa parametrizable y componentes transversales.

## Pantallas

- `MovimientosInventarioView`
- `ComprasEntradasView`
- `VentasInternasView`

## Componentes usados

- `ModuleScaffold`
- `AppCard`
- `AppButton`
- `InfoPanel`
- `FormGrid`
- `AppDialog`

## Principio UX

Cada pantalla debe resolver una tarea principal:

- movimientos: corregir o ajustar stock;
- compras: recibir mercadería;
- salidas: descontar stock por control interno.

No se mezclan flujos para evitar errores humanos.
