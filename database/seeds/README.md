# Seeds

Datos iniciales y datos inventados de presentación.

## Cliente real limpio

`database/sql/seeds/V001__seed_inicial_cliente.sql` contiene datos mínimos para operar:

- configuración base editable;
- licencia pendiente;
- categorías comunes;
- marca `Sin marca`;
- proveedor `Proveedor no especificado`;
- unidades de medida;
- parámetros base;
- ayuda contextual.

No contiene productos, compras, ventas, caja ni clientes ficticios.

## Presentación

`database/sql/seeds/V001__seed_presentacion.sql` contiene datos inventados para mostrar el sistema con información cargada. Debe ejecutarse sobre la base separada de presentación mediante:

```bat
scripts\db-seed-presentacion.bat
```

## Reset de presentación

`database/sql/seeds/V001__reset_presentacion.sql` elimina solo datos identificados como presentación. El script oficial crea respaldo previo y pide confirmación.
