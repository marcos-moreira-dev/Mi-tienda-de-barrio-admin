# Manejo de errores UI

## Principio

Mostrar mensaje simple al usuario y guardar detalle técnico en log local.

## Reglas

- No cerrar la app ante error recuperable.
- No mostrar stack traces al cliente.
- Proponer acción: reintentar, revisar datos, crear respaldo o contactar soporte.
- Para operaciones críticas, dejar registro en `logs/`.
