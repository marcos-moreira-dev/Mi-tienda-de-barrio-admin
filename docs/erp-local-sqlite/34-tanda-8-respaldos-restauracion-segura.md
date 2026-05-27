# Tanda 8 — Respaldos y restauración segura

## Objetivo

Fortalecer el módulo de respaldos para que MiTienda ERP Local SQLite no restaure cualquier archivo a ciegas.

En una aplicación local basada en SQLite, los respaldos no son un accesorio: son parte central de la confianza del producto.

## Cambios implementados

### Validación antes de crear respaldo

Antes de copiar la base activa, `RespaldoService` valida:

- que la base exista;
- que tenga cabecera SQLite válida;
- que pase `PRAGMA integrity_check`;
- que pase `PRAGMA foreign_key_check`.

Si la base activa no pasa validación, no se crea el respaldo.

### Validación antes de restaurar

Antes de restaurar un archivo seleccionado, `RespaldoService` valida:

- que el archivo exista;
- que no sea la misma base activa;
- que tenga cabecera `SQLite format 3`;
- que pase `PRAGMA integrity_check`;
- que pase `PRAGMA foreign_key_check`.

Si falla alguna validación, la restauración se rechaza con mensaje humano.

### Respaldo preventivo

Antes de sobrescribir la base activa, se crea un respaldo tipo `PRE_RESTAURACION`.

Si ese respaldo preventivo falla, la restauración no continúa.

### Registro de restauración

Cuando se restaura correctamente, el repositorio marca el respaldo como `RESTAURADO` si ese archivo existe en el historial.

### Auditoría

Se conectó el módulo de respaldos con `AuditoriaService`.

Eventos auditados:

- `CREAR_RESPALDO`;
- `VALIDAR_RESPALDO`;
- `RESTAURAR_RESPALDO`.

La auditoría no debe tumbar operaciones normales, pero deja trazabilidad para eventos críticos.

## Archivos modificados

- `core/application/respaldo/RespaldoService.java`
- `core/application/respaldo/RespaldoRepository.java`
- `core/infrastructure/respaldo/SqliteRespaldoRepository.java`
- `core/domain/respaldo/TipoRespaldo.java`
- `bootstrap/AppBootstrap.java`

## Scripts agregados

- `scripts/validate-respaldos-seguros.bat`
- `scripts/validate-respaldos-seguros.ps1`

## Validaciones realizadas

- Compilación del core sin JavaFX con `javac --release 21`.
- Ejecución de schema + seed + smoke check en SQLite temporal.
- `PRAGMA integrity_check` correcto.
- `PRAGMA foreign_key_check` correcto.
- Validación de cabecera SQLite en copia de respaldo temporal.

## Decisión

La restauración ya no es una copia directa insegura.

Ahora sigue este flujo:

1. validar respaldo seleccionado;
2. crear respaldo preventivo;
3. restaurar base;
4. marcar respaldo como restaurado;
5. auditar el evento;
6. pedir reinicio de la aplicación.
