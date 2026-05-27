# Modelo de datos general

## Núcleo

- ConfiguracionNegocio.
- Producto.
- Categoria.
- Marca.
- UnidadMedida.
- Proveedor.
- Compra.
- DetalleCompra.
- MovimientoInventario.
- VentaInterna.
- DetalleVentaInterna.
- RespaldoSistema.
- LicenciaSistema.

## Opcionales

- LoteProducto.
- CajaDiaria.
- MovimientoCaja.
- ClienteFiado.
- CuentaPorCobrar.
- Abono.
- MermaRetiro.

## Producto

Campos sugeridos: código interno, nombre, categoría, marca, unidad, presentación, stock actual, stock mínimo, stock objetivo, precio compra, precio venta, foto, perecible, refrigerado, maneja lote, maneja vencimiento, activo.

## Relaciones

- Categoría 1:N Producto.
- Marca 1:N Producto.
- Proveedor 1:N Compra.
- Compra 1:N DetalleCompra.
- Producto 1:N MovimientoInventario.
- Producto 1:N LoteProducto.
- VentaInterna 1:N DetalleVentaInterna.

## Decisiones

Proveedor debe ser entidad desde V1. Lote y vencimiento son opcionales por producto. Caja y fiado se activan solo si el cliente los necesita.
