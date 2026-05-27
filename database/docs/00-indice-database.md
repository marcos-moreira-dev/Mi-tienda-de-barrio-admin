# Índice de base de datos

## Propósito

Centralizar la documentación de la base local SQLite de **Mi tienda de barrio admin**.

## 1. Planificación de BD

```text
database/docs/1_planificacion_bd/
```

- `01_bd_canonica_y_objetivo.md`
- `02_modelo_conceptual_y_objetos_nucleares.md`
- `03_diccionario_de_datos_candidato.md`
- `04_reglas_de_integridad_y_trazabilidad.md`
- `05_decisiones_previas_a_sql.md`
- `06_politica_de_roles_ownership_y_triggers.md`
- `07_matriz_conceptual_logico_fisico.md`

## 2. Implementación de BD

```text
database/docs/2_implementacion_bd/
```

- `10_informe_sql_v001_bruto.md`
- `15_informe_sql_1fn_legacy.md`
- `20_informe_de_normalizacion.md`
- `25_matriz_dependencias_funcionales.md`
- `30_informe_sql_3fn_oficial.md`
- `40_politica_de_seeds_y_datos_iniciales.md`
- `50_checklist_de_transicion_a_migraciones.md`
- `60_politica_de_ambientes_backup_restore_y_reset_local.md`
- `70_cierre_v001_sqlite.md`

## 3. Revisión de BD

```text
database/docs/3_revision_bd/
```

- `01_revision_exhaustiva_v001.md`

## SQL

```text
database/sql/archivo/V001__schema_1fn_legacy.sql
database/sql/archivo/V001__schema_bruto.sql
database/sql/migrations/V001__schema_3fn_oficial.sql
database/sql/seeds/V001__seed_presentacion.sql
database/sql/seeds/V001__reset_presentacion.sql
database/sql/checks/V001__smoke_check.sql
```

## Fuente oficial

La fuente oficial ejecutable de V001 es:

```text
database/sql/migrations/V001__schema_3fn_oficial.sql
```

Los archivos 1FN/bruto son legacy documental.
