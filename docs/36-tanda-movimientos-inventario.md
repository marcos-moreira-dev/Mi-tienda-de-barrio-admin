# Tanda 4 — Movimientos de inventario

## Objetivo

Implementar el módulo de movimientos de inventario como vertical slice de trazabilidad.

## Alcance implementado

- Dominio `MovimientoInventario`.
- Enum `TipoMovimientoInventario`.
- Puerto `MovimientoInventarioRepository`.
- Servicio `MovimientoInventarioService`.
- Adaptador `SqliteMovimientoInventarioRepository`.
- Vista JavaFX `MovimientosInventarioView`.
- Registro de ajustes positivos.
- Registro de ajustes negativos.
- Registro de correcciones.
- Registro de merma.
- Registro de retiro por vencimiento.
- Actualización transaccional de `producto.stock_actual`.
- Inserción automática en `movimiento_inventario`.
- Inserción en `merma_retiro` cuando corresponde.
- Acceso desde la shell principal.

## Decisión de diseño

Compras y ventas internas no se registran manualmente aquí. Cada una tiene su propio flujo vertical para evitar mezclar responsabilidades.

## Trazabilidad humana

Todo ajuste manual exige motivo. Responsable y observación son opcionales, pero están disponibles para registrar contexto humano.

## Estado

Implementado como base funcional. Falta validar localmente con Maven y uso real de interfaz.
