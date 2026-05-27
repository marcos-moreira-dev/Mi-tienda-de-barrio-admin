# Tanda 14 — Cartera local completa

## Decisión

Esta tanda conecta cartera, ventas pagadas, abonos y cuentas por pagar con caja diaria.

La regla funcional queda así:

```text
Caja = dinero real que entró o salió.
Cartera = deuda pendiente por cobrar o por pagar.
```

## Cambios de base de datos

Se agregaron vínculos hacia caja en:

```text
venta_pago.movimiento_caja_id
abono.movimiento_caja_id
pago_proveedor.movimiento_caja_id
```

Estos campos permiten rastrear qué movimiento de caja representa cada cobro o pago real.

## Código agregado

Paquetes nuevos:

```text
core/domain/cartera
core/application/cartera
core/infrastructure/cartera
```

Clases principales:

```text
CarteraLocalService
CarteraLocalRepository
SqliteCarteraLocalRepository
RegistroAbonoConCaja
RegistroPagoProveedorConCaja
RegistroVentaPagadaEnCaja
CarteraCajaResultado
```

## Flujos cubiertos

### Venta pagada conectada a caja

```text
venta_pago pendiente
→ movimiento_caja INGRESO
→ venta_pago.movimiento_caja_id
→ recalcular caja
```

### Abono de fiado conectado a caja

```text
cuenta_por_cobrar ABIERTA
→ movimiento_caja INGRESO
→ abono
→ abono.movimiento_caja_id
→ actualizar saldo de cuenta
→ recalcular caja
```

### Pago a proveedor conectado a caja

```text
cuenta_por_pagar PENDIENTE/PARCIAL
→ movimiento_caja EGRESO
→ pago_proveedor
→ pago_proveedor.movimiento_caja_id
→ actualizar saldo de cuenta
→ recalcular caja
```

## Validación

Se agregó:

```text
scripts/validate-cartera-local.bat
scripts/validate-cartera-local.ps1
```

El script valida:

- columnas de vínculo hacia caja;
- venta pagada conectada como ingreso;
- abono conectado como ingreso;
- pago proveedor conectado como egreso;
- saldo esperado de caja;
- claves foráneas.
