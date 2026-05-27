# Migraciones locales

## Propósito

Definir cómo evolucionará la base SQLite sin romper instalaciones existentes.

## Convención sugerida

Usar archivos numerados:

```text
V001__schema_inicial.sql
V002__catalogos_iniciales.sql
V003__modulo_caja.sql
V004__modulo_fiado.sql
```

## Tabla de control

Crear una tabla de versión, por ejemplo:

```text
schema_version
```

Debe registrar:

- versión aplicada;
- nombre de migración;
- fecha;
- resultado;
- hash opcional;
- observación.

## Reglas

- No editar una migración ya aplicada en cliente.
- Crear una nueva migración para cada cambio posterior.
- Hacer respaldo antes de aplicar migraciones.
- Aplicar migraciones en orden.
- Ejecutar validación posterior.
- Registrar errores en logs.

## Operaciones delicadas

Son delicadas:

- borrar columnas;
- cambiar tipo de dato;
- modificar significado de un campo;
- dividir una tabla;
- fusionar entidades;
- alterar reglas de stock.

## Política para este proyecto

La V1 debe tener pocas migraciones y muy bien documentadas. Primero estabilidad, luego evolución.
