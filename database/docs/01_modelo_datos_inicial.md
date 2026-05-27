# Modelo de datos inicial

## Entidades indispensables

### Producto

Campos: id, codigo_interno, nombre, categoria_id, marca_id opcional, unidad_medida_id, presentacion, stock_actual, stock_minimo, stock_objetivo, precio_compra_referencia, precio_venta, maneja_lote, maneja_vencimiento, perecible, refrigerado, foto_ruta, activo, creado_en, actualizado_en.

### Categoría

Campos: id, nombre, descripcion, activa.

### Marca

Campos: id, nombre, activa. Puede degradarse a texto si el cliente no necesita filtro por marca.

### UnidadMedida

Campos: id, nombre, abreviatura, permite_decimal.

### Proveedor

Campos: id, nombre, telefono, direccion, observacion, activo.

### Compra

Campos: id, proveedor_id, fecha, numero_comprobante, total_estimado, observacion.

### DetalleCompra

Campos: id, compra_id, producto_id, cantidad, costo_unitario, lote, fecha_vencimiento, aceptado, motivo_rechazo, observacion.

### MovimientoInventario

Campos: id, producto_id, tipo, cantidad, stock_anterior, stock_nuevo, motivo, referencia_tipo, referencia_id, observacion, fecha.

### VentaInterna

Campos: id, fecha, total, metodo_pago, numero_comprobante_externo, observacion, anulada.

### DetalleVentaInterna

Campos: id, venta_interna_id, producto_id, cantidad, precio_unitario, subtotal.

### ConfiguracionNegocio

Campos: id, nombre_comercial, responsable, ruc_opcional, telefono, direccion, actividad, observaciones_documentales.

### LicenciaSistema

Campos: id, codigo_hash, tipo, vigente_desde, vigente_hasta, estado, ultima_validacion, modo_limitado.

### RespaldoSistema

Campos: id, fecha, ruta, tipo, resultado, observacion.

## Entidades recomendadas/opcionales

- LoteProducto.
- CajaDiaria.
- MovimientoCaja.
- ClienteFiado.
- CuentaPorCobrar.
- Abono.
- MermaRetiro.

## Regla

No obligar lote/vencimiento a productos que no lo necesitan. Activar por producto.
