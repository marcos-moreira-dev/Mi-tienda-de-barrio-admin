# Tanda 21 — Preflight y release local

## Objetivo

Esta tanda deja una puerta única de validación final para MiTienda ERP Local SQLite antes de entregar o seguir empaquetando el producto.

## Archivos agregados

- `scripts/release-preflight.bat`
- `scripts/release-preflight.ps1`
- `scripts/package-release-local.bat`
- `scripts/package-release-local.ps1`

## Archivos actualizados

- `test.bat`

## Qué valida el preflight

- Archivos obligatorios de scripts, SQL, semillas, checks y bootstrap.
- Sincronía entre SQL de `database/sql` y SQL embebido en `desktop/src/main/resources/db`.
- Ausencia de cercas Markdown dentro de archivos SQL.
- Presencia de las tablas principales de la V001 consolidada.
- Migrador apuntando a `V001__schema_erp_local_sqlite_consolidado.sql`.
- Validación de `PRAGMA integrity_check` y `PRAGMA foreign_key_check` en el migrador.
- `test.bat` ejecutando todas las validaciones principales.
- Advertencias si aparecen carpetas o archivos típicos de build fuera de `.diagnostics`.

## Cómo probar todo

Desde la raíz del proyecto:

```powershell
.\test.bat
```

Desde `scripts`:

```powershell
.\test.bat
```

## Cómo generar un ZIP local de release

Desde la raíz:

```powershell
.\scripts\package-release-local.bat
```

El ZIP se genera en:

```text
.dist/
```

## Decisión

El `test.bat` queda como la puerta única de retroalimentación rápida. Si falla, el log queda en `.diagnostics/logs`.
