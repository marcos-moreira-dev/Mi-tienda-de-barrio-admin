# Tanda 17 — Contabilidad básica

## Decisión

Se agrega contabilidad básica local para MiTienda ERP Local SQLite.

Este módulo no reemplaza revisión profesional ni obligaciones contables o tributarias formales. Sirve para registrar plan de cuentas mínimo, asientos manuales, detalle debe/haber y validación de cuadre.

## Tablas agregadas

- `tipo_cuenta_contable`
- `cuenta_contable`
- `tipo_diario_contable`
- `asiento_contable`
- `asiento_contable_detalle`
- `plantilla_asiento`
- `plantilla_asiento_detalle`
- `regla_contable_evento`

## Código agregado

Paquetes:

- `core/domain/contabilidad`
- `core/application/contabilidad`
- `core/infrastructure/contabilidad`

Clases principales:

- `ContabilidadBasicaService`
- `ContabilidadBasicaRepository`
- `SqliteContabilidadBasicaRepository`
- `CuentaContable`
- `AsientoContable`
- `AsientoContableDetalle`
- `CrearAsientoContableSolicitud`
- `AsientoContableDetalleSolicitud`

## Reglas implementadas

- Un asiento debe tener al menos dos líneas.
- Cada línea usa una cuenta contable válida.
- Cada línea debe tener valor en debe o en haber, pero no ambos.
- El asiento debe cuadrar: total debe = total haber.
- Los asientos no se borran; se anulan.
- Cada asiento puede guardar origen operativo mediante `origen_tipo` y `origen_id`.

## Seed base

Se agregan tipos de cuenta, diarios contables y un plan de cuentas mínimo:

- Caja
- Cuentas por cobrar
- Inventario
- Cuentas por pagar
- Capital del negocio
- Ingresos por ventas internas
- Gastos operativos
- Pérdidas por merma
- Costo de ventas

## Validación

Se agrega:

- `scripts/validate-contabilidad-basica.bat`
- `scripts/validate-contabilidad-basica.ps1`

También se integra en `test.bat`.

La validación prueba:

- existencia de tablas;
- plan de cuentas base;
- diario general;
- asiento cuadrado aceptado;
- asiento descuadrado rechazado;
- línea con debe y haber al mismo tiempo rechazada;
- `foreign_key_check`;
- `integrity_check`.
