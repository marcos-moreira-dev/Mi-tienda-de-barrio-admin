# Tanda 11 — Compras avanzadas

## Decisión

La compra deja de ser únicamente una entrada simple de un solo producto y queda preparada para operar como documento local con múltiples detalles.

Esta tanda no implementa todavía caja ni contabilidad automática. Es el puente funcional para que después compras se conecte con caja, cuentas por pagar y contabilidad básica.

## Cambios principales

Se agregaron:

- `cuenta_por_pagar`;
- `pago_proveedor`;
- comando de compra avanzada;
- detalles múltiples por compra;
- servicio para registrar pago a proveedor;
- consulta de cuentas por pagar pendientes.

## Reglas implementadas

- Una compra avanzada debe tener al menos un detalle.
- Cada detalle debe tener producto, cantidad positiva y costo unitario no negativo.
- La compra a crédito crea una cuenta por pagar por el total de la compra.
- El pago a proveedor no puede superar el saldo pendiente.
- Si el saldo llega a cero, la cuenta queda `PAGADA`.
- Si queda saldo, la cuenta queda `PARCIAL`.
- Cada entrada de mercadería actualiza stock y deja movimiento de inventario.

## Tablas nuevas

- `cuenta_por_pagar`
- `pago_proveedor`

## Código agregado

- `RegistroCompraAvanzada`
- `DetalleCompraAvanzada`
- `CuentaPorPagar`
- `PagoProveedor`
- `RegistroPagoProveedor`

## Validación

Ejecutar:

```powershell
.\scripts\validate-compras-avanzadas.bat
```

También conviene ejecutar:

```powershell
.\scripts\validate-core-no-javafx.bat
.\scripts\validate-sql-local.bat
```

## Pendiente futuro

- conectar compra pagada con caja;
- conectar pago a proveedor con caja;
- generar asiento contable cuando exista contabilidad básica;
- pantalla avanzada de compras con varios productos;
- reporte de cuentas por pagar.
