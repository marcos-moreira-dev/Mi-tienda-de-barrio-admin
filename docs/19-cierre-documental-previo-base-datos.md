# Cierre documental previo a base de datos

## Estado

La documentación funcional y arquitectónica ya define una V1 suficientemente clara para pasar a diseño físico de base de datos en una siguiente tanda.

Este cierre no contiene SQL. Su objetivo es congelar criterios antes de crear `V001__schema_inicial.sql` o equivalente.

## Fuentes de verdad para la siguiente fase

Leer en este orden:

1. `README.md`
2. `docs/00-indice-general.md`
3. `docs/03-alcance-vendible.md`
4. `docs/18-matriz-consistencia-v1.md`
5. `docs/06-modelo-de-datos-general.md`
6. `database/docs/01-decisiones-de-base-de-datos.md`
7. `database/docs/03-tablas-y-campos.md`
8. `docs/modulos/00-indice-modulos.md`

## Criterios congelados

### Producto

- Nombre visible: **Mi tienda de barrio admin**.
- Producto local de escritorio, no demo visible.
- Rubro base: tienda/despensa/bazar/minimarket pequeño.

### Arquitectura

- Desktop JavaFX.
- SQLite local.
- Una computadora.
- Reportes PDF y CSV.
- Licencia local renovable.
- Carpetas locales para datos, respaldos, reportes, imágenes, logs, configuración y licencia.

### Módulos V1

- Configuración del negocio.
- Productos.
- Categorías, marcas y unidades.
- Proveedores.
- Compras/entradas.
- Inventario/movimientos.
- Salidas/ventas internas.
- Reportes.
- Respaldos/restauración.
- Licencia.
- Ayuda contextual.

### Módulos opcionales locales

- Caja diaria.
- Fiado/cuentas por cobrar.
- Checklist de recepción.
- Lote/vencimiento avanzado.

### Fuera de V1

- Facturación electrónica.
- Integración SRI.
- Nube obligatoria.
- App móvil.
- Multi-PC.
- Multi-sucursal.
- Contabilidad formal.
- Balanzas.
- Lectores de código de barras obligatorios.

## Decisiones para base de datos

1. Usar nombres de tablas en singular o plural de forma consistente. Recomendación: singular en español técnico (`producto`, `proveedor`, `compra`).
2. Usar `id` entero/autoincremental como clave primaria local.
3. Usar `estado` para activo/inactivo cuando una entidad no deba borrarse físicamente.
4. No eliminar productos con historial; desactivar.
5. Separar `stock_actual`, `stock_minimo` y `stock_objetivo`.
6. Toda operación que cambie stock debe generar `movimiento_inventario`.
7. Lote/vencimiento debe existir como capacidad opcional por producto.
8. Caja y fiado pueden modelarse desde el inicio si se desea estructura completa, pero deben poder ocultarse en UI.
9. Licencia y configuración deben quedar en tablas propias para evitar valores quemados en código.
10. Respaldos deben registrarse como eventos, aunque el archivo físico esté fuera de la DB.

## Criterio para pasar a V001

Se puede pasar a base de datos cuando se acepte:

- lista de tablas iniciales;
- campos obligatorios mínimos;
- relaciones principales;
- reglas de borrado/desactivación;
- estrategia de migraciones;
- datos iniciales mínimos;
- rutas de respaldo y restauración.

## Nota

La siguiente fase debe producir primero documentación de base física y luego SQL. No saltar directamente a código JavaFX.
