# 63. Cierre R5/R6 — repositorios SQLite y separación de formularios

## Objetivo

Cerrar la tanda de refactor arquitectónico iniciada en R5/R6 sin cambiar el comportamiento de negocio ni el esquema SQLite.

La prioridad fue reducir malos olores visibles:

- repositorios SQLite con infraestructura común;
- pantallas JavaFX menos monolíticas;
- formularios y listados separados por responsabilidad;
- mantenimiento más sencillo para próximas tandas.

## R5 — Repositorios SQLite

Todos los adaptadores `Sqlite*Repository` pasan a heredar de `SqliteRepositorySupport`.

La clase base ahora centraliza:

- `SqliteConnectionFactory` protegido;
- `JdbcExecutor` protegido;
- `SqliteTransactionManager` protegido;
- utilidades comunes de normalización de texto;
- utilidades comunes para nulos, `Long` nullable y `BigDecimal`.

### Decisión importante

No se eliminó el SQL explícito. Este sistema no usa ORM. La idea es mantener repositorios legibles para un proyecto local, pero reducir repetición de infraestructura.

Los flujos transaccionales complejos, como compras, ventas internas, caja, movimientos y fiado, pueden seguir usando conexión JDBC explícita cuando necesitan controlar varios pasos atómicos. La diferencia es que ahora dependen de una base común y no de campos privados repetidos en cada repositorio.

## R6 — Separación de formularios y listados

Se agregaron panes especializados por módulo:

### Compras

- `ComprasListPane`
- `ComprasFormPane`

### Salidas / ventas internas

- `VentasListPane`
- `VentasFormPane`

### Movimientos de inventario

- `MovimientosListPane`
- `MovimientosFormPane`

### Caja diaria

- `CajaListPane`
- `CajaFormPane`

### Fiado

- `FiadoListPane`
- `FiadoFormPane`

Productos ya venía separado desde la tanda anterior con:

- `ProductoFilterPane`
- `ProductoListPane`
- `ProductoFormPane`
- `ProductoFormData`

## Regla aplicada

Las vistas principales deben actuar como orquestadoras:

- configurar datos;
- escuchar eventos;
- llamar servicios de aplicación;
- delegar composición visual a panes especializados.

Los panes especializados deben encargarse de la estructura visual del módulo, no de la lógica de negocio.

## Validación realizada

Se validó compilación del núcleo sin JavaFX usando `javac --release 21` sobre:

- `core/**`
- `shared/**`
- `bootstrap/**`

La validación Maven completa debe ejecutarse en la máquina local con Eclipse Temurin 21 y Maven Toolchain:

```bat
scripts\validate-desktop.bat
```

## Pendiente recomendado

La siguiente tanda natural es R7:

- dividir `app.css` por capas;
- separar tokens visuales, shell, componentes y módulos;
- dejar el estilo del sidebar y scrollbars más mantenible.
