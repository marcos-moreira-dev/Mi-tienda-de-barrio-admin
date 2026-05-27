# Tanda 10 — Caja diaria opcional

## Objetivo
Implementar una caja operativa local para negocios que desean registrar apertura, ingresos, egresos y cierre diario sin convertir el sistema en contabilidad formal.

## Alcance implementado
- Dominio: `CajaDiaria`, `MovimientoCaja`, `EstadoCajaDiaria`, `TipoMovimientoCaja`, `MetodoPagoCaja`.
- Core embebido: `CajaDiariaService` y `CajaDiariaRepository`.
- Infraestructura: `SqliteCajaDiariaRepository`.
- UI JavaFX: `CajaDiariaView`.
- Navegación desde la carcasa parametrizable.

## Decisiones
- Caja es módulo opcional.
- No reemplaza contabilidad ni obligaciones tributarias.
- El cierre calcula diferencia entre saldo esperado y saldo contado.
- Los movimientos manuales exigen descripción para trazabilidad humana.

## Pendiente específico
- Integración automática opcional entre ventas internas y caja.
- Reporte PDF de cierre.
