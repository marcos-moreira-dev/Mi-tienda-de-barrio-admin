# 03. Diccionario de datos candidato

## Propósito

Registrar las tablas candidatas y campos principales antes de consolidar el SQL físico oficial.
Este documento no reemplaza al SQL; funciona como puente humano entre el modelo conceptual y la migración V001.

## Criterio de lectura

- **Indispensable:** entra en V001 oficial.
- **Recomendado:** entra en V001 si no rompe simplicidad.
- **Opcional:** puede existir en V001 como capacidad apagada o secundaria.
- **Avanzado:** no entra en V001 local.

## Tablas indispensables

### configuracion_negocio

Propósito: datos básicos del local.

Campos candidatos:

- id
- nombre_comercial
- ruc
- responsable
- telefono
- direccion
- actividad
- moneda
- observacion
- created_at
- updated_at

Nivel: indispensable.

### categoria

Propósito: clasificación principal de productos.

Campos candidatos:

- id
- nombre
- descripcion
- estado
- created_at
- updated_at

Nivel: indispensable.

### marca

Propósito: normalizar marcas cuando aportan búsqueda, filtro o reporte.

Campos candidatos:

- id
- nombre
- descripcion
- estado
- created_at
- updated_at

Nivel: recomendado, incluido en V001.

### unidad_medida

Propósito: controlar unidades como unidad, libra, kg, litro, funda, caja.

Campos candidatos:

- id
- nombre
- abreviatura
- permite_decimales
- estado
- created_at
- updated_at

Nivel: indispensable.

### proveedor

Propósito: registrar fuentes de abastecimiento.

Campos candidatos:

- id
- nombre
- telefono
- whatsapp
- direccion
- observacion
- estado
- created_at
- updated_at

Nivel: indispensable.

### producto

Propósito: objeto central del inventario.

Campos candidatos:

- id
- codigo_interno
- nombre
- descripcion
- categoria_id
- marca_id
- unidad_medida_id
- proveedor_principal_id
- presentacion
- precio_compra_referencia
- precio_venta
- stock_actual
- stock_minimo
- stock_objetivo
- maneja_lote
- maneja_vencimiento
- perecible
- refrigerado
- ruta_foto
- estado
- observacion
- created_at
- updated_at

Nivel: indispensable.

### compra

Propósito: cabecera de recepción o compra de mercadería.

Campos candidatos:

- id
- proveedor_id
- fecha_compra
- numero_comprobante
- tipo_comprobante
- total_estimado
- estado
- observacion
- created_at
- updated_at

Nivel: indispensable.

### detalle_compra

Propósito: detalle de productos comprados.

Campos candidatos:

- id
- compra_id
- producto_id
- lote_id
- cantidad
- costo_unitario
- subtotal
- codigo_lote
- fecha_vencimiento
- observacion
- created_at
- updated_at

Nivel: indispensable.

### movimiento_inventario

Propósito: historial funcional del stock.

Campos candidatos:

- id
- producto_id
- lote_id
- tipo_movimiento
- cantidad
- stock_anterior
- stock_nuevo
- fecha_movimiento
- referencia_tipo
- referencia_id
- motivo
- responsable_texto
- observacion
- created_at
- updated_at

Nivel: indispensable.

### venta_interna

Propósito: salida operativa no tributaria.

Campos candidatos:

- id
- fecha_venta
- total
- metodo_pago
- numero_referencia
- estado
- advertencia_tributaria_aceptada
- observacion
- created_at
- updated_at

Nivel: indispensable si se desea descontar stock por salida diaria.

### detalle_venta_interna

Propósito: productos descontados en una venta interna.

Campos candidatos:

- id
- venta_interna_id
- producto_id
- lote_id
- cantidad
- precio_unitario
- subtotal
- observacion
- created_at
- updated_at

Nivel: indispensable si existe venta interna.

## Tablas recomendadas

### lote_producto

Propósito: gestionar lote/vencimiento cuando aplica.

Nivel: recomendado.

### merma_retiro

Propósito: registrar pérdida, daño, vencimiento o retiro.

Nivel: recomendado.

### licencia_sistema

Propósito: licencia local renovable y modo limitado ético.

Nivel: indispensable para el modelo comercial.

### respaldo_sistema

Propósito: historial de respaldos y restauraciones.

Nivel: indispensable.

### ayuda_contextual

Propósito: ayuda integrada por módulo y clave.

Nivel: recomendado.

### parametro_configuracion

Propósito: configuración flexible local.

Nivel: recomendado.

## Tablas opcionales V001

### caja_diaria

Propósito: control de efectivo diario.

Nivel: opcional V001, recomendado para tiendas con empleados o caja separada.

### movimiento_caja

Propósito: ingresos, egresos y ajustes de caja.

Nivel: opcional V001.

### cliente_fiado

Propósito: registrar clientes con deuda informal.

Nivel: opcional.

### cuenta_por_cobrar

Propósito: deuda pendiente por fiado.

Nivel: opcional.

### abono

Propósito: pagos parciales de una deuda.

Nivel: opcional.

## Elementos avanzados excluidos

- sucursal;
- usuario con roles complejos;
- factura electrónica;
- comprobante SRI;
- asiento contable;
- integración bancaria;
- dispositivo externo;
- auditoría forense;
- sincronización nube.

## Cierre del diccionario candidato

El diccionario candidato confirma que V001 puede ser suficientemente completa sin convertirse en ERP.
La 3FN oficial conserva el núcleo operativo y deja caja/fiado como capacidades opcionales documentadas.
