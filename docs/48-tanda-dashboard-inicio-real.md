# Tanda 13 — Dashboard de inicio real

## Objetivo

Reemplazar el inicio placeholder por un tablero operativo real, calculado desde SQLite, sin romper la naturaleza autocontenida de la aplicación.

## Implementado

- `DashboardResumen`.
- `DashboardRepository`.
- `DashboardService`.
- `SqliteDashboardRepository`.
- `DashboardView`.
- Integración en `MainShellView`.

## Indicadores incluidos

- Productos activos.
- Productos bajo stock.
- Productos agotados.
- Productos por comprar.
- Productos próximos a vencer.
- Ventas internas del día.
- Compras del día.
- Estado de caja diaria.
- Último respaldo registrado.
- Estado de licencia.

## Decisión UX/UI

El dashboard es de solo lectura. Debe seguir disponible aunque la licencia esté en modo limitado, porque ayuda al usuario a entender el estado de sus datos sin registrar operaciones nuevas.

## Trazabilidad

Flujo aplicado:

```text
DashboardView
→ DashboardService
→ DashboardRepository
→ SqliteDashboardRepository
→ SQLite
```

