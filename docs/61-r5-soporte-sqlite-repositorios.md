# R5 – Soporte común para repositorios SQLite

## Objetivo

Reducir repetición en los adaptadores SQLite sin introducir ORM, framework nuevo ni cambiar el esquema de base de datos.

El sistema sigue usando SQL explícito porque es una aplicación local, autocontenida y orientada a cliente final. La limpieza consiste en centralizar el patrón repetido de:

- abrir conexión;
- preparar sentencia;
- enlazar parámetros;
- mapear `ResultSet`;
- cerrar recursos;
- envolver errores en `InfrastructureException`.

## Nuevas piezas

Se agregó el paquete:

```text
core/infrastructure/sqlite/
```

con estas clases:

```text
RowMapper.java
StatementBinder.java
JdbcExecutor.java
SqliteRepositorySupport.java
SqliteTransactionManager.java
```

## Repositorios migrados en esta tanda

Para no hacer un refactor riesgoso de todos los repositorios a la vez, esta tanda migra como referencia inicial:

```text
SqliteCategoriaRepository
SqliteProveedorRepository
```

Estos repositorios ahora extienden `SqliteRepositorySupport` y usan `JdbcExecutor` para operaciones de consulta, lectura por ID, actualización e inserción con ID generado.

## Decisiones

- No se cambia el esquema SQLite.
- No se cambia la interfaz pública de los repositorios.
- No se cambia la capa de servicios.
- No se cambia el comportamiento funcional.
- No se agrega Hibernate, JPA ni Spring.
- El patrón queda listo para migrar progresivamente productos, compras, salidas, movimientos, caja y fiado.

## Riesgo

Bajo-medio. Aunque el patrón es pequeño, los repositorios son sensibles porque cualquier error afecta persistencia local. Por eso se migraron solo dos repositorios de bajo riesgo como primera muestra.

## Validación sugerida

```bat
scripts\validate-desktop.bat
```

Después probar manualmente:

1. Abrir Catálogos.
2. Crear/editar/desactivar una categoría.
3. Abrir Proveedores.
4. Crear/editar/desactivar un proveedor.
5. Cerrar y abrir la app para confirmar persistencia.
