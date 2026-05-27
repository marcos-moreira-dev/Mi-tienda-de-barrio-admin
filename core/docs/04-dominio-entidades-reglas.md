# Dominio, entidades y reglas

## Entidades principales

Producto, Categoría, Marca, Unidad de medida, Proveedor, Compra, Detalle de compra, Venta interna, Detalle de venta interna, Movimiento de inventario, Lote de producto, Merma/retiro, Caja diaria, Fiado/cuenta por cobrar, Configuración del negocio, Licencia local y Respaldo.

## Reglas principales

- El stock no debe quedar negativo salvo decisión explícita documentada.
- Una compra genera movimientos de entrada.
- Una venta interna genera movimientos de salida.
- Una merma genera salida con motivo.
- Un producto bajo se define por `stock_actual <= stock_minimo`.
- La cantidad sugerida a comprar se calcula contra `stock_objetivo` cuando exista.
- Los vencimientos son opcionales por producto/lote.
- Una licencia vencida no debe borrar ni secuestrar datos.
