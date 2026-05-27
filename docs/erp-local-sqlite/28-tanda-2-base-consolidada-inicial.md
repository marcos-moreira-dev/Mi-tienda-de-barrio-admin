# Tanda 2 — Base consolidada inicial

## Objetivo

Preparar una primera base SQLite consolidada, compatible con la decisión de no arrastrar muchas migraciones históricas porque el sistema todavía no ha sido usado por clientes reales.

## Cambios realizados

Se agregó la migración canónica inicial:

```text
database/sql/migrations/V001__schema_erp_local_sqlite_consolidado.sql
desktop/src/main/resources/db/migrations/V001__schema_erp_local_sqlite_consolidado.sql
```

La nueva V001 parte de la estructura 3FN actual y agrega una tabla formal:

```text
schema_version
```

También se actualizó el migrador local para usar esta migración consolidada:

```text
desktop/src/main/java/com/marcosmoreira/mitiendadebarrio/admin/core/infrastructure/sqlite/LocalDatabaseMigrator.java
```

## Alcance real de esta tanda

Esta tanda no mete todavía todas las tablas ERP futuras. Su propósito es cambiar el eje del proyecto:

```text
antes: V001 3FN oficial histórica
ahora: V001 ERP local consolidada como base canónica inicial
```

Las tablas ERP grandes se integrarán por tandas, pero el resultado final debe quedar compactado, no fragmentado en una cadena larga de migraciones.

## Decisión

La base final del producto debe terminar en una V001 consolidada potente, con seeds separados.
