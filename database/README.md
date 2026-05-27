# Base de datos — Mi tienda de barrio admin

## Propósito

Esta carpeta contiene la planificación, implementación y revisión formal de la base local SQLite del sistema.

El sistema usa SQLite porque la V001 está pensada para:

- una sola computadora;
- operación local;
- funcionamiento sin internet obligatorio;
- respaldo manual guiado;
- instalación sencilla;
- transición futura a PostgreSQL + backend centralizado si el negocio crece.

## Estructura

```text
database/
├── docs/
│   ├── 00-indice-database.md
│   ├── 1_planificacion_bd/
│   ├── 2_implementacion_bd/
│   └── 3_revision_bd/
├── migrations/
├── samples/
├── seeds/
└── sql/
    ├── archivo/
    ├── checks/
    ├── migrations/
    └── seeds/
```

## Archivos SQL importantes

### Legacy / formalidad

```text
database/sql/archivo/V001__schema_1fn_legacy.sql
database/sql/archivo/V001__schema_bruto.sql
```

Estos archivos no son el esquema final. Se conservan para documentar el paso desde 1FN hacia 3FN.

### Migración oficial

```text
database/sql/migrations/V001__schema_3fn_oficial.sql
```

Este archivo es la fuente oficial de la V001.

### Seeds

```text
database/sql/seeds/V001__seed_presentacion.sql
database/sql/seeds/V001__reset_presentacion.sql
```

Estos archivos sirven para datos de presentación interna.
No deben llamarse demo en la UI ni en entregables visibles al cliente.

### Revisión

```text
database/sql/checks/V001__smoke_check.sql
```

Consulta auxiliar para revisar estructura y conteos.

## Estado V001

La V001 incluye:

- configuración del negocio;
- categorías, marcas y unidades;
- proveedores;
- productos;
- lotes/vencimientos opcionales;
- compras y detalle;
- ventas internas y detalle;
- movimientos de inventario;
- mermas/retiros;
- caja opcional;
- fiado opcional;
- licencia local;
- respaldos;
- ayuda contextual;
- parámetros locales.

## Regla de implementación

Toda operación que cambie stock debe pasar por servicios transaccionales:

- compra;
- venta interna;
- ajuste;
- merma/retiro;
- corrección.

No se debe modificar `producto.stock_actual` directamente desde pantallas sueltas.

## Cierre

La base V001 está lista para pasar a inicialización SQLite, repositorios/DAO y servicios de aplicación cuando se inicie la implementación.
