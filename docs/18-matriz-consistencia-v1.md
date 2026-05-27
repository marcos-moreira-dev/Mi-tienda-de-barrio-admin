# Matriz de consistencia V1 — Mi tienda de barrio admin

## Propósito

Este documento cruza la promesa comercial, los módulos, las entidades y las pantallas para evitar que la primera versión vendible se infle o se contradiga.

La V1 debe ser suficientemente útil para una tienda/despensa real, pero no debe convertirse en ERP, POS tributario ni sistema multiusuario.

## Criterio de clasificación

- **V1 obligatoria:** entra desde la primera versión vendible.
- **V1 configurable:** entra, pero puede activarse/desactivarse según el negocio.
- **Opcional local:** puede venderse como módulo adicional local.
- **Avanzado:** requiere otra arquitectura, otro presupuesto o transición.
- **Fuera:** no pertenece a este producto inicial.

## Matriz principal

| Área | Necesidad del negocio | Módulo | Entidades | Nivel V1 | Nota |
|---|---|---|---|---|---|
| Datos del negocio | Identificar local, responsable y rutas | Configuración | ConfiguracionNegocio | V1 obligatoria | Sirve para encabezados de reportes, rutas y soporte. |
| Catálogo | Registrar qué vende la tienda | Productos | Producto, Categoria, Marca, UnidadMedida | V1 obligatoria | Producto es el centro del sistema. |
| Stock | Saber cuánto hay | Productos / Inventario | Producto, MovimientoInventario | V1 obligatoria | Stock actual debe cambiar solo por operaciones registradas. |
| Bajo stock | Saber qué comprar | Reportes | Producto | V1 obligatoria | Requiere stock mínimo y preferiblemente stock objetivo. |
| Compras | Registrar entrada de mercadería | Compras | Compra, DetalleCompra, Proveedor | V1 obligatoria | Es el flujo que alimenta inventario. |
| Proveedores | Recordar contactos y precios | Proveedores | Proveedor, Compra | V1 obligatoria | Proveedor no debe quedar como simple texto en V1. |
| Salidas | Descontar stock sin facturación | Salidas / ventas internas | VentaInterna, DetalleVentaInterna, MovimientoInventario | V1 obligatoria | Debe aclarar que no reemplaza comprobantes tributarios. |
| Ajustes | Corregir conteos | Inventario | MovimientoInventario | V1 obligatoria | Todo ajuste requiere motivo y observación. |
| Merma | Registrar pérdida, daño o vencimiento | Inventario | MermaRetiro, MovimientoInventario | V1 configurable | Obligatorio si vende perecibles. |
| Vencimientos | Alertar productos por caducar | Productos / Reportes | LoteProducto, Producto | V1 configurable | No imponer a productos que no lo necesitan. |
| Caja diaria | Resumen de efectivo | Caja | CajaDiaria, MovimientoCaja | Opcional local | Útil, pero aumenta disciplina y soporte. |
| Fiado | Controlar cuentas pendientes | Fiado | ClienteFiado, CuentaPorCobrar, Abono | Opcional local | Muy común en barrio, pero amplía alcance. |
| Respaldo | Evitar pérdida de datos | Respaldos | RespaldoSistema | V1 obligatoria | Debe estar visible y ser fácil. |
| Licencia | Proteger sostenibilidad del producto | Licencia | LicenciaSistema | V1 obligatoria | Modo limitado ético, nunca secuestro de datos. |
| Multi-PC | Usar varias computadoras | Transición | — | Avanzado | Requiere backend centralizado/base centralizada. |
| SRI | Facturación electrónica | Transición | — | Avanzado | Requiere integración y obligaciones tributarias. |
| Nube/app móvil | Acceso remoto | Transición | — | Avanzado | Otro producto, otro presupuesto. |

## Decisiones oficiales de V1

1. **V1 sí incluye ventas internas**, pero no facturación electrónica.
2. **V1 sí incluye proveedores**, porque son parte del valor operativo.
3. **V1 sí incluye reportes PDF y CSV**, porque el reporte de productos por comprar es clave.
4. **V1 sí incluye respaldos**, porque SQLite local sin backup visible es riesgoso.
5. **V1 sí incluye licencia local**, porque el producto debe ser sostenible.
6. **V1 no incluye varias computadoras**, aunque el cliente lo pida como “algo sencillo”.
7. **V1 no incluye SRI**, para evitar responsabilidad tributaria no presupuestada.
8. **Caja y fiado quedan como opcionales locales**, no como base obligatoria.
9. **Lote/vencimiento es configurable**, no universal.
10. **Código de barras queda preparado como campo**, pero no como flujo obligatorio.

## Reglas de consistencia

- Si una pantalla modifica stock, debe generar movimiento de inventario.
- Si una compra registra productos, debe actualizar stock en transacción.
- Si una salida/venta interna registra productos, debe descontar stock en transacción.
- Si un producto queda por debajo del stock mínimo, debe aparecer en reportes de bajo stock.
- Si un producto tiene lote/vencimiento activado, debe poder aparecer en reportes de próximos a vencer.
- Si la licencia vence, el sistema debe permitir consulta, respaldo y exportación.
- Si el cliente pide facturación electrónica, se activa conversación de transición, no se mete en V1.
