# Scripts de base de datos

## Propósito

Preparar scripts para migrar, respaldar, restaurar y validar SQLite local.

## Scripts previstos

- `db-migrate.bat`
- `db-seed.bat`
- `db-backup.bat`
- `db-restore.bat`
- `db-integrity-check.bat`

## Reglas

- Crear respaldo antes de migrar.
- Validar integridad después de restaurar.
- No sobrescribir backups sin confirmación.
- Registrar fecha y ruta del archivo generado.
