# Tanda 4 — Mejorar migrador local

## Objetivo

Fortalecer el migrador local de MiTienda ERP Local SQLite para que no reejecute el esquema de forma peligrosa, reconozca la versión instalada y falle con un mensaje humano cuando encuentre una base antigua o incompatible.

## Decisiones aplicadas

- La base canónica esperada es `V001`.
- La migración esperada es `V001__schema_erp_local_sqlite_consolidado.sql`.
- Como el sistema no ha sido usado por clientes reales, no se arrastra una cadena larga de migraciones.
- El migrador no intenta ser Flyway; solo instala la V001 consolidada y protege contra estados peligrosos.

## Cambios técnicos

Se actualizó:

```text
LocalDatabaseMigrator.java
```

Ahora el migrador:

1. Detecta si existe `schema_version`.
2. Si existe, valida que la versión sea `V001`.
3. Si no existe `schema_version` pero la base tiene tablas de usuario, detiene el arranque con un mensaje claro.
4. Si la base está vacía, ejecuta la V001 consolidada.
5. Registra la versión esperada en `schema_version`.
6. Ejecuta `PRAGMA integrity_check`.
7. Ejecuta `PRAGMA foreign_key_check`.

También se actualizó:

```text
DatabaseHealthCheck.java
```

Ahora informa la versión de esquema instalada y reporta alertas de integridad si aparecen.

## Tests / validaciones agregadas

Se agregaron:

```text
scripts/validate-migrator-local.bat
scripts/validate-migrator-local.ps1
```

La validación revisa:

- constantes esperadas del migrador;
- uso de `schema_version`;
- protección contra bases antiguas sin versión;
- `PRAGMA integrity_check`;
- `PRAGMA foreign_key_check`;
- existencia de `schema_version` en el SQL;
- registro de `V001` en el seed;
- smoke check con `schema_version`;
- ejecución del contrato SQL en una base SQLite temporal.

## Validación realizada durante la tanda

Se validó en este entorno:

```text
javac --release 21 del subconjunto no JavaFX: OK
contrato estático del migrador: OK
schema + seed + smoke en SQLite temporal: OK
integrity_check: OK
foreign_key_check: OK
```

## Pendiente

La siguiente tanda debe modularizar la shell JavaFX para evitar que `MainShellView` siga creciendo mientras se agregan módulos ERP locales.
