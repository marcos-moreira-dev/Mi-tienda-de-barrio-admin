# Transacciones SQLite

Deben ejecutarse en transacción:

- registrar compra con detalle;
- registrar venta interna con detalle;
- registrar ajuste de inventario;
- registrar merma/retiro;
- restaurar respaldo;
- aplicar migración;
- reset de datos de presentación;
- renovar licencia si afecta estado local.

## Recomendaciones

- `PRAGMA foreign_keys = ON`.
- Usar `busy_timeout`.
- Respaldar antes de restauraciones o migraciones.
- No abrir la base desde una carpeta sincronizada de nube.
- No compartir el archivo SQLite por red para multi-PC.
