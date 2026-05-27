# Movimientos, compras y salidas — Vertical slices V12

## Decisión

Se implementaron tres flujos con responsabilidad única:

- Movimientos: ajustes manuales y trazabilidad.
- Compras: entradas de mercadería.
- Salidas: ventas internas no tributarias.

## Regla transversal

Toda operación que cambia stock debe:

1. Leer stock actual.
2. Calcular stock nuevo.
3. Validar que no sea negativo.
4. Actualizar `producto.stock_actual`.
5. Insertar registro en `movimiento_inventario`.
6. Ejecutarse dentro de una transacción SQLite.

## Referencias de implementación

- `MovimientoInventarioService`
- `CompraService`
- `VentaInternaService`
- `SqliteMovimientoInventarioRepository`
- `SqliteCompraRepository`
- `SqliteVentaInternaRepository`
