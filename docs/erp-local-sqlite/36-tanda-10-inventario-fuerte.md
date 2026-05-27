# Tanda 10 — Inventario fuerte

## Objetivo

Fortalecer el inventario local sin romper los flujos actuales de productos, compras, ventas internas y movimientos manuales.

## Implementado

Se agregaron tablas para:

- catálogo de tipos de movimiento de inventario;
- conteos físicos de inventario;
- detalles de conteo;
- ajustes formales de inventario;
- detalles de ajuste conectados con movimientos de inventario.

También se agregó infraestructura Java local:

- dominio de inventario fuerte;
- `InventarioFuerteRepository`;
- `InventarioFuerteService`;
- `SqliteInventarioFuerteRepository`;
- conexión en `AppBootstrap` y `AppContext`.

## Decisión técnica

Compras y ventas siguen usando sus flujos actuales. El módulo de inventario fuerte se reserva para conteos, ajustes, mermas y correcciones con trazabilidad.

## Validación

Se agregó:

```powershell
.\scripts\validate-inventario-fuerte.bat
```

La validación comprueba tablas, seed, conteo, ajuste, movimiento de inventario y claves foráneas.
