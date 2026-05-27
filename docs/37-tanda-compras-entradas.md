# Tanda 5 — Compras / Entradas

## Objetivo

Implementar la entrada de mercadería como flujo oficial para aumentar stock.

## Alcance implementado

- Dominio `Compra`.
- Enum `TipoComprobanteCompra`.
- Comando `RegistroCompraSimple`.
- Puerto `CompraRepository`.
- Servicio `CompraService`.
- Adaptador `SqliteCompraRepository`.
- Vista JavaFX `ComprasEntradasView`.
- Registro de compra simple de un producto.
- Asociación opcional de proveedor.
- Comprobante referencial: sin comprobante, nota, factura u otro.
- Registro opcional de lote.
- Registro opcional de vencimiento.
- Actualización transaccional de stock.
- Creación automática de movimiento `ENTRADA_COMPRA`.
- Creación opcional de `lote_producto`.
- Actualización de precio de compra referencial.

## Decisión de diseño

La compra no es contabilidad. Es una recepción operativa de mercadería para inventario local.

## Estado

Implementado como flujo simple. Futuro: detalle multiproducto y checklist formal de recepción.
