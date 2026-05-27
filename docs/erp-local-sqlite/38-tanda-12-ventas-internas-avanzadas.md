# Tanda 12 — Ventas internas avanzadas

## Objetivo

Fortalecer el flujo de ventas internas sin convertirlo en facturación electrónica ni comprobante tributario autorizado.

## Implementado

- Venta interna con varios productos.
- Registro de pagos de venta interna pagada.
- Método de pago `FIADO`.
- Cuenta por cobrar generada desde venta fiada.
- Anulación controlada de venta interna.
- Reverso de stock al anular.
- Movimiento de inventario de corrección al anular.
- Auditoría desde `VentaInternaService`.

## Tablas agregadas

- `venta_pago`
- `anulacion_venta`

## Tablas extendidas

- `venta_interna` ahora acepta `cliente_fiado_id` y método de pago `FIADO`.

## Código agregado

- `RegistroVentaInternaAvanzada`
- `DetalleVentaInternaAvanzada`
- `VentaPago`
- `RegistroAnulacionVentaInterna`
- `AnulacionVentaInterna`

## Código modificado

- `MetodoPagoVentaInterna`
- `VentaInternaService`
- `VentaInternaRepository`
- `SqliteVentaInternaRepository`
- `AppBootstrap`
- `V001__schema_erp_local_sqlite_consolidado.sql`
- `V001__smoke_check.sql`

## Regla comercial

La venta interna sigue siendo un control operativo interno.

No reemplaza factura, nota de venta autorizada ni comprobante electrónico SRI.

## Validación

Script agregado:

```powershell
.\scripts\validate-ventas-avanzadas.bat
```

Valida:

- venta con varios productos;
- pago registrado;
- venta fiada;
- cuenta por cobrar creada;
- anulación controlada;
- foreign keys correctas.

## Pendiente

La conexión completa con caja se hará en tandas posteriores:

- Tanda 13 — Caja y gastos.
- Tanda 14 — Cartera local completa.
