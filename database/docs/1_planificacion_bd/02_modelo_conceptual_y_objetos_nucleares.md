# 02. Modelo conceptual y objetos nucleares

## Propósito del documento

Este documento fija el **modelo conceptual previo a SQL** de **Mi tienda de barrio admin**.
Su objetivo es dejar clara la semántica del dominio antes de bajar a tablas, claves foráneas, migraciones y normalización.

Aquí no se modelan pantallas ni se congela todavía el esquema físico final.
Lo que se congela aquí es:

- cuáles son los objetos nucleares del negocio;
- cómo se relacionan;
- qué fronteras semánticas existen;
- qué información debe sobrevivir al paso del tiempo;
- qué cosas son obligatorias en V001 y qué cosas son opcionales.

## Aclaración importante

La base de datos no modela el flujo visual de la app como si fuera un wizard.
Lo que modela son los **objetos**, los **eventos operativos**, los **estados**, los **historiales**, los **snapshots económicos** y la **trazabilidad mínima** que una tienda necesita para operar con más orden.

Dicho simple:

- el flujo de pantalla influye en la BD;
- pero la BD no debe depender de una pantalla específica;
- la BD debe sobrevivir aunque mañana se rediseñe el desktop JavaFX.

## Ejes conceptuales del sistema

### Eje de configuración local

Representa la identidad mínima del negocio y el comportamiento local del sistema.
Aquí viven:

- configuración del negocio;
- parámetros locales;
- licencia;
- respaldos;
- ayuda contextual.

### Eje de catálogo e inventario

Representa lo que el negocio vende, compra, clasifica y necesita reponer.
Aquí viven:

- producto;
- categoría;
- marca;
- unidad de medida;
- proveedor;
- lote de producto;
- movimiento de inventario;
- merma o retiro.

### Eje operativo de compra y salida

Representa los hechos que cambian el stock y producen trazabilidad.
Aquí viven:

- compra;
- detalle de compra;
- venta interna;
- detalle de venta interna;
- ajuste;
- salida;
- retiro por vencimiento o daño.

### Eje financiero operativo simple

No es contabilidad formal.
Representa control interno simple para negocios pequeños.
Aquí viven:

- caja diaria;
- movimiento de caja;
- cliente fiado;
- cuenta por cobrar;
- abono.

## Objeto principal del dominio

El objeto principal es **Producto**.

El producto concentra:

- identidad comercial;
- clasificación;
- unidad;
- precios de referencia;
- stock actual;
- umbrales de reposición;
- flags de perecible/refrigerado;
- capacidades opcionales de lote y vencimiento;
- estado activo/inactivo.

## Objeto histórico central

El objeto histórico central es **MovimientoInventario**.

No basta con saber el stock actual.
El sistema debe saber por qué cambió:

- entrada por compra;
- salida por venta interna;
- ajuste positivo;
- ajuste negativo;
- merma;
- retiro por vencimiento;
- corrección.

## Objetos nucleares

### ConfiguracionNegocio

Representa los datos mínimos del local para encabezados de reportes, configuración inicial y operación.
No es asesor legal ni reemplaza documentos oficiales.

### ParametroConfiguracion

Permite manejar parámetros locales flexibles sin modificar el esquema por cada ajuste menor.
Ejemplos: carpeta de respaldos, días de alerta de vencimiento, formato de reportes.

### Categoria

Clasificación principal del producto.
Debe existir como entidad porque se usa para filtrar, reportar y ordenar inventario.

### Marca

Normaliza marcas cuando aportan búsqueda o reporte.
En tiendas pequeñas puede parecer opcional, pero como el producto apunta a ser base reutilizable, conviene mantenerla como entidad.

### UnidadMedida

Define cómo se mide el producto: unidad, libra, kilogramo, paquete, funda, caja, litro, etc.
Incluye si permite decimales.

### Proveedor

Representa persona, distribuidor, mayorista o fuente de abastecimiento.
Debe existir como entidad porque afecta compras, historial de precios, reposición y observaciones.

### Producto

Objeto central del sistema.
Debe permanecer entendible y no convertirse en una tabla gigante de todo el negocio.

### LoteProducto

Capacidad opcional para productos que manejan lote, vencimiento o trazabilidad por recepción.
No todos los productos necesitan lote.
Debe existir para no forzar campos sanitarios en productos que no los requieren.

### Compra

Cabecera de una entrada de mercadería.
Representa una compra o recepción, incluso si no existe factura formal.

### DetalleCompra

Producto específico dentro de una compra.
Permite cantidad, costo unitario, subtotal, lote y vencimiento asociado.

### VentaInterna

Salida operativa no tributaria.
Sirve para descontar stock y controlar operación interna.
No reemplaza factura, nota de venta ni comprobante SRI.

### DetalleVentaInterna

Producto vendido o retirado dentro de una venta interna.
Permite conservar precio unitario y subtotal como snapshot económico.

### MovimientoInventario

Historial de cambios de stock.
Debe existir aunque haya compra y venta interna, porque también hay ajustes, mermas, retiros y correcciones.

### MermaRetiro

Registro específico de pérdida, daño, vencimiento, consumo interno o retiro.
Permite trazabilidad más clara que un simple ajuste negativo.

### CajaDiaria

Módulo opcional para control de efectivo.
No es contabilidad formal.

### MovimientoCaja

Ingreso, egreso o ajuste dentro de una caja diaria.

### ClienteFiado

Cliente informal al que se le permite deuda.
Debe quedar como módulo opcional porque aumenta alcance y soporte.

### CuentaPorCobrar

Deuda generada por una venta interna u operación fiada.

### Abono

Pago parcial o total de una cuenta por cobrar.

### LicenciaSistema

Estado local de licencia, vencimiento y modo limitado.
Debe permitir proteger el negocio del desarrollador sin secuestrar datos del cliente.

### RespaldoSistema

Historial de respaldos generados o restaurados.

### AyudaContextual

Contenido de ayuda embebido en la aplicación.
Se alinea con el patrón de ayuda contextual de los proyectos de referencia.

## Relaciones conceptuales nucleares

- Una categoría tiene muchos productos.
- Una marca puede tener muchos productos.
- Una unidad de medida puede usarse en muchos productos.
- Un proveedor puede ser proveedor principal de muchos productos.
- Un proveedor puede tener muchas compras.
- Una compra tiene muchos detalles de compra.
- Un producto puede aparecer en muchos detalles de compra.
- Un producto puede tener muchos lotes.
- Un lote pertenece a un producto.
- Una venta interna tiene muchos detalles.
- Un producto puede aparecer en muchos detalles de venta interna.
- Un producto puede tener muchos movimientos de inventario.
- Un lote puede tener muchos movimientos de inventario.
- Una merma/retiro pertenece a un producto y opcionalmente a un lote.
- Una caja diaria tiene muchos movimientos de caja.
- Un cliente fiado tiene muchas cuentas por cobrar.
- Una cuenta por cobrar puede tener muchos abonos.
- Una venta interna puede originar una cuenta por cobrar.

## Fronteras semánticas

### Producto vs lote

`producto` describe el artículo estable.
`lote_producto` describe una partida concreta con recepción, proveedor, costo y vencimiento.

### Compra vs movimiento de inventario

`compra` explica el evento de entrada comercial.
`movimiento_inventario` conserva el impacto sobre stock.

### Venta interna vs comprobante tributario

`venta_interna` es control operativo.
No modela obligación tributaria ni transmisión al SRI.

### Merma/retiro vs ajuste

Un ajuste corrige stock.
Una merma/retiro explica pérdida, vencimiento, daño o salida no comercial.

### Caja vs contabilidad

`caja_diaria` controla efectivo operativo.
No reemplaza contabilidad ni libro diario.

### Fiado vs crédito formal

`cliente_fiado`, `cuenta_por_cobrar` y `abono` modelan deuda informal de tienda.
No modelan crédito financiero formal.

## Objetos que no entran en V001

No se modelan:

- sucursales;
- facturas electrónicas;
- comprobantes SRI;
- asientos contables;
- integración bancaria;
- tienda online;
- app móvil;
- roles empresariales complejos;
- auditoría forense;
- balanzas;
- lector de código de barras obligatorio;
- varias computadoras simultáneas.

## Decisión final del modelo conceptual

La V001 debe ser una base local seria, no un ERP.
Debe poder sostener una tienda real de una sola computadora y dejar una ruta clara de transición cuando el negocio exija PostgreSQL + backend centralizado.
