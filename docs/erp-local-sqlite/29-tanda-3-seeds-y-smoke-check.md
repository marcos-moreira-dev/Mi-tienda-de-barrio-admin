# Tanda 3 — Seeds y smoke check inicial

## Objetivo

Alinear los datos iniciales y la validación SQL con la nueva V001 consolidada.

## Cambios realizados

Se actualizó el seed inicial para registrar la versión instalada:

```text
database/sql/seeds/V001__seed_inicial_cliente.sql
desktop/src/main/resources/db/seeds/V001__seed_inicial_cliente.sql
```

Se actualizó el smoke check para incluir:

```text
schema_version
```

y se agregó una copia en resources:

```text
desktop/src/main/resources/db/checks/V001__smoke_check.sql
```

También se agregaron scripts:

```text
scripts/validate-sql-local.bat
scripts/validate-sql-local.ps1
```

Estos scripts validan en una base SQLite temporal:

```text
schema consolidado
seed inicial
smoke check
PRAGMA foreign_key_check
```

## Uso recomendado en Windows

Desde la raíz del proyecto:

```powershell
.\scripts\validate-sql-local.bat
```

## Resultado en esta tanda

La validación local de SQL se probó en una base SQLite temporal y no reportó errores de claves foráneas.

## Decisión

Cada ampliación importante del modelo SQLite debe venir acompañada de seed y smoke check actualizados.
