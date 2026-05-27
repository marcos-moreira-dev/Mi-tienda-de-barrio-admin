# Tanda V001 — Base de datos SQLite

## Estado

Se inició formalmente la fase de base de datos siguiendo el procedimiento del proyecto de referencia.

## Procedimiento seguido

1. Crear bloque de planificación documental.
2. Crear bloque de implementación documental.
3. Definir SQL bruto de referencia.
4. Definir SQL oficial 3FN.
5. Definir seed de presentación.
6. Definir reset de presentación.
7. Mantener separación entre documentación, migraciones y seeds.

## Archivos clave creados

```text
database/docs/1_planificacion_bd/
database/docs/2_implementacion_bd/
database/sql/archivo/V001__schema_bruto.sql
database/sql/migrations/V001__schema_3fn_oficial.sql
database/sql/seeds/V001__seed_presentacion.sql
database/sql/seeds/V001__reset_presentacion.sql
```

## Decisiones cerradas

- SQLite es la base oficial de V001.
- `producto` es el núcleo del dominio.
- `movimiento_inventario` es el historial funcional principal.
- Compras y ventas internas quedan separadas.
- Venta interna no reemplaza facturación.
- Lote/vencimiento entra físicamente como capacidad opcional.
- Caja y fiado entran físicamente como capacidades opcionales.
- Licencia local se incluye desde V001 con enfoque ético.
- Backups quedan como parte del núcleo, no como extra.

## Siguiente paso recomendado

Validar el SQL con SQLite y luego iniciar estructura fuente JavaFX/repositories.
