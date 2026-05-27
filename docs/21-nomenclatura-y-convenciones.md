# Nomenclatura y convenciones

## Propósito

Evitar que el proyecto mezcle nombres personales, nombres de demo o estilos inconsistentes.

## Nombre visible del producto

- **Mi tienda de barrio admin**

No usar en UI ni entregables del cliente:

- demo
- mock
- sample
- prueba
- prototipo
- sandbox

## Nombres técnicos recomendados

### Proyecto raíz

`mi-tienda-de-barrio-admin`

### Paquete Java sugerido

`com.marcosmoreira.mitiendadebarrioadmin`

### Carpetas principales

- `docs/`
- `database/`
- `desktop/`
- `scripts/`
- `assets/`
- `runtime-data-example/`

### Tablas SQLite

Usar snake_case en español técnico:

- `producto`
- `categoria`
- `proveedor`
- `movimiento_inventario`
- `venta_interna`
- `detalle_venta_interna`

### Clases Java

Usar PascalCase:

- `Producto`
- `ProductoRepository`
- `RegistrarCompraUseCase`
- `ProductosController`
- `ProductoFormView`

### Casos de uso

Usar verbo + objeto:

- `RegistrarProducto`
- `EditarProducto`
- `RegistrarCompra`
- `GenerarReporteProductosPorComprar`
- `CrearRespaldoManual`

### Archivos Markdown

Usar numeración + kebab-case:

- `01-vision-del-producto.md`
- `02-investigacion-del-rubro.md`

Cuando una carpeta tiene documentos internos de módulo, usar bloques:

- `00_proposito.md`
- `10_decisiones_arquitectonicas.md`
- `40_casos_de_uso.md`
- `50_flujos_operativos.md`
- `60_manual_de_usuario.md`
- `70_componentes_acciones_y_tooltips.md`
- `75_errores_mensajes_y_feedback.md`
- `80_pruebas_del_modulo.md`

## Convención de estados

Usar mayúsculas técnicas en base de datos si se decide string:

- `ACTIVO`
- `INACTIVO`
- `ANULADO`
- `COMPLETADO`

Para UI, traducir a lenguaje humano:

- Activo
- Inactivo
- Anulado
- Completado

## Convención de mensajes

Los mensajes al usuario deben ser cortos y accionables.

Ejemplo correcto:

> No se puede registrar la salida porque el stock disponible es menor que la cantidad solicitada.

Ejemplo incorrecto:

> Error SQL constraint failed.

## Convención nueva — core embebido

Para V1 usar:

- `core embebido`
- `servicio de aplicación`
- `caso de uso`
- `repositorio SQLite`
- `OperationResult`
- `controlador JavaFX`
- `runtime local`

Evitar en V1:

- `backend`
- `endpoint`
- `controller REST`
- `ApiResponse`
- `servidor local`
- `Spring Boot`
- `localhost`

La palabra backend solo debe aparecer para describir una transición futura a sistema avanzado.
