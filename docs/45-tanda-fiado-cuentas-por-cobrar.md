# Tanda 11 — Fiado / cuentas por cobrar opcional

## Objetivo
Agregar soporte básico para negocios que venden fiado y necesitan registrar clientes, deudas, abonos y saldos pendientes.

## Alcance implementado
- Dominio: `ClienteFiado`, `CuentaPorCobrar`, `Abono`, estados.
- Core embebido: `FiadoService` y `FiadoRepository`.
- Infraestructura: `SqliteFiadoRepository`.
- UI JavaFX: `FiadoCuentasView`.
- Navegación desde la carcasa parametrizable.

## Decisiones
- Fiado es módulo opcional.
- No es cartera financiera avanzada.
- Los abonos actualizan saldo y cierran la cuenta si llega a cero.
- El módulo evita depender de cuaderno o memoria.

## Pendiente específico
- Asociar una venta interna directamente a una cuenta por cobrar.
- Reporte PDF de cuentas pendientes.
