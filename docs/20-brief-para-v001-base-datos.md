# Brief para V001 de base de datos

## Objetivo

Preparar la siguiente fase: diseño físico inicial de SQLite para **Mi tienda de barrio admin**.

Este documento sirve como puente entre documentación y migración SQL. Todavía no es el script definitivo.

## Tablas candidatas V1 obligatorias

- `configuracion_negocio`
- `categoria`
- `marca`
- `unidad_medida`
- `proveedor`
- `producto`
- `compra`
- `detalle_compra`
- `movimiento_inventario`
- `venta_interna`
- `detalle_venta_interna`
- `merma_retiro`
- `licencia_sistema`
- `respaldo_sistema`

## Tablas candidatas configurables/opcionales

- `lote_producto`
- `caja_diaria`
- `movimiento_caja`
- `cliente_fiado`
- `cuenta_por_cobrar`
- `abono`
- `ayuda_contextual`
- `parametro_configuracion`

## Catálogos mínimos

- tipos de movimiento: entrada, salida, ajuste, merma, vencimiento, corrección.
- estados generales: activo, inactivo.
- motivos de merma/retiro: dañado, vencido, pérdida, consumo interno, corrección.
- métodos de pago opcionales: efectivo, transferencia, otro.
- tipos de valor de configuración: texto, número, booleano, ruta, fecha.

## Campos transversales recomendados

Para entidades principales:

- `id`
- `estado`
- `created_at`
- `updated_at`
- `observacion`

Para operaciones:

- `fecha_operacion`
- `usuario_operacion` o `responsable_texto` si aún no hay usuarios reales.
- `observacion`

## Reglas de integridad

- `producto.categoria_id` debe existir.
- `producto.unidad_medida_id` debe existir.
- `producto.marca_id` puede ser nulo.
- `detalle_compra.compra_id` debe existir.
- `detalle_compra.producto_id` debe existir.
- `detalle_venta_interna.venta_interna_id` debe existir.
- `detalle_venta_interna.producto_id` debe existir.
- `movimiento_inventario.producto_id` debe existir.
- Stock no debe quedar negativo salvo que se decida permitirlo por configuración, y en V1 se recomienda no permitirlo.

## Transacciones obligatorias

Deben ejecutarse en transacción:

- registrar compra con detalles;
- registrar venta interna con detalles;
- registrar ajuste de inventario;
- registrar merma/retiro;
- restaurar respaldo;
- aplicar migración.

## Datos iniciales sugeridos

- categorías base: víveres, bebidas, limpieza, aseo personal, snacks, lácteos, carnes/refrigerados, papelería, bazar, otros.
- unidades: unidad, caja, paquete, funda, botella, litro, kilogramo, libra, gramo.
- marcas: genérica/sin marca.
- configuración inicial del negocio con campos vacíos editables.
- ayudas contextuales mínimas para productos, compras, reportes, respaldos y licencia.

## Pendientes antes de escribir SQL

- Confirmar si caja y fiado entran físicamente desde V001 o desde V002.
- Confirmar si `lote_producto` entra desde V001 como tabla lista pero UI opcional.
- Confirmar convención final de timestamps (`created_at`/`updated_at` texto ISO-8601 o integer epoch).
- Confirmar si se usará `CHECK` extensivo o catálogos para estados.
- Confirmar estrategia de `PRAGMA foreign_keys=ON` en arranque.
