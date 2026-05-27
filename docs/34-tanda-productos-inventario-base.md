# Tanda 3 — Productos / Inventario base

## Objetivo

Implementar el catálogo maestro de productos y stock base, todavía sin movimientos transaccionales avanzados.

## Decisiones aplicadas

- Producto se conecta con categoría, unidad de medida y proveedor principal.
- Marca es opcional.
- Stock actual, mínimo y objetivo quedan desde el inicio porque sostienen reportes de bajo stock y productos por comprar.
- Lote y vencimiento se modelan como capacidades del producto, no como obligación universal.
- Perecible y refrigerado quedan como banderas operativas para futuras alertas.

## Archivos principales

```text
desktop/src/main/java/.../core/domain/producto/Producto.java
desktop/src/main/java/.../core/domain/producto/EstadoProducto.java
desktop/src/main/java/.../core/application/producto/ProductoRepository.java
desktop/src/main/java/.../core/application/producto/ProductoService.java
desktop/src/main/java/.../core/infrastructure/producto/SqliteProductoRepository.java
desktop/src/main/java/.../desktop/ui/screens/productos/ProductosInventarioView.java
```

## Responsabilidad única

- `Producto` contiene datos y reglas derivadas simples como bajo stock y cantidad sugerida.
- `ProductoService` valida reglas de negocio.
- `ProductoRepository` define el contrato de persistencia.
- `SqliteProductoRepository` mapea la tabla `producto` y sus relaciones.
- `ProductosInventarioView` presenta el CRUD local.

## Alcance funcional

- Buscar por nombre, código o categoría.
- Crear producto.
- Editar producto.
- Activar/desactivar producto.
- Registrar precios, stock y proveedor principal.
- Marcar capacidades de lote, vencimiento, perecible y refrigerado.

## Pendiente futuro

- Selección visual de imagen.
- Reporte de bajo stock.
- Movimientos de inventario.
- Compras/entradas con actualización transaccional de stock.
- Salidas/ventas internas.
