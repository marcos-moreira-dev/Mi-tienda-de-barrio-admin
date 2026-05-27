# 06. Política de roles, ownership y triggers

## Propósito

Ajustar el criterio del proyecto de referencia a SQLite local.

## Roles y ownership

En SQLite local no existe el mismo modelo de roles que PostgreSQL. La política se traduce a:

- ownership de archivo;
- ubicación controlada de la base;
- permisos del sistema operativo;
- ruta de datos bajo carpeta del producto;
- backups guiados;
- y licencia local del sistema.

## Archivo oficial

La base local debe vivir en una ruta controlada de la aplicación, por ejemplo:

```text
runtime/data/mi-tienda-de-barrio-admin.sqlite
```

## Triggers permitidos

Se permiten triggers pequeños para actualizar `updated_at`.

No se permiten triggers que oculten lógica compleja como cálculo completo de stock, cierre de caja, vencimiento automático de licencia o control tributario.
