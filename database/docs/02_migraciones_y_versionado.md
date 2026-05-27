# Migraciones y versionado

## Convención

```text
V001__crear_tablas_base.sql
V002__crear_modulo_compras.sql
V003__crear_modulo_ventas_internas.sql
V004__crear_respaldo_licencia.sql
```

## Tabla de control sugerida

`schema_version` con: version, descripcion, aplicada_en, checksum.

## Regla

Toda migración debe ser idempotente cuando sea posible y estar respaldada por documentación breve.
