# 04. Reglas de integridad y trazabilidad

## Propósito

Definir reglas que la base de datos debe proteger desde V001.

## Reglas principales

- Todo producto debe tener categoría y unidad de medida.
- Marca y proveedor principal pueden ser nulos.
- El stock no debe ser negativo en V001.
- Stock mínimo y precios no deben ser negativos.
- Stock objetivo debe ser mayor o igual al stock mínimo cuando exista.
- Las cantidades en compras, ventas, movimientos, abonos y mermas deben ser positivas.
- Compras, ventas internas, ajustes y mermas deben ejecutarse en transacción.
- Toda operación que cambie stock debe generar `movimiento_inventario` desde la capa de aplicación.
- Venta interna no reemplaza factura ni comprobante tributario.
- La licencia vencida no debe borrar ni secuestrar datos.
- Aun con licencia vencida se debe permitir consulta, respaldo y exportación.

## Trazabilidad funcional

Deben quedar historizados:

- compras;
- detalles de compra;
- movimientos de inventario;
- ventas internas;
- detalles de ventas internas;
- mermas/retiros;
- respaldos;
- cambios relevantes de licencia.

## Timestamps

Todas las tablas principales tienen `created_at` y `updated_at`.
