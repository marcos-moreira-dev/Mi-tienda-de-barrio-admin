# Tanda 13 — Caja y gastos

## Objetivo

Fortalecer caja como centro de tesorería local del ERP SQLite, agregando catálogo de movimientos, formas de pago, gastos operativos y arqueos.

## Cambios de base de datos

Se agregaron las tablas:

- `tipo_movimiento_caja`
- `forma_pago_local`
- `tipo_gasto`
- `gasto_operativo`
- `arqueo_caja`

También se sembraron catálogos base para movimientos de caja, formas de pago y tipos de gasto.

## Cambios de código

Se agregaron modelos de dominio para:

- `TipoGasto`
- `GastoOperativo`
- `RegistroGastoOperativo`
- `ArqueoCaja`
- `RegistroArqueoCaja`

Se extendieron:

- `CajaDiariaService`
- `CajaDiariaRepository`
- `SqliteCajaDiariaRepository`

## Flujos implementados

### Registrar gasto operativo

El servicio valida caja abierta, tipo de gasto activo, monto positivo y descripción obligatoria.

Luego crea:

1. movimiento de caja de egreso;
2. gasto operativo ligado al movimiento;
3. recálculo de caja;
4. evento de auditoría.

### Registrar arqueo de caja

El servicio valida caja abierta y saldo contado no negativo.

Luego crea un arqueo con:

- saldo del sistema;
- saldo contado;
- diferencia;
- responsable;
- observación.

## Validación

Se agregó:

- `scripts/validate-caja-gastos.bat`
- `scripts/validate-caja-gastos.ps1`

El script valida tablas, seeds, gasto operativo, arqueo y claves foráneas.

## Decisión

Caja ya no debe ser solo una tabla auxiliar. Desde esta tanda queda preparada como centro de dinero real del ERP local.

La integración automática de ventas, abonos y pagos a proveedor con caja se completará en la siguiente tanda de cartera local completa.
