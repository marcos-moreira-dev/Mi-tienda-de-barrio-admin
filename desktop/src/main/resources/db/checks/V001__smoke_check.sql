-- Mi tienda de barrio admin
-- V001 - smoke check de estructura SQLite
--
-- Este archivo es auxiliar para revisión manual.
-- Debe ejecutarse después de V001__schema_erp_local_sqlite_consolidado.sql.

PRAGMA foreign_keys = ON;

SELECT 'schema_version' AS tabla, COUNT(*) AS filas FROM schema_version
UNION ALL SELECT 'configuracion_negocio' AS tabla, COUNT(*) AS filas FROM configuracion_negocio
UNION ALL SELECT 'usuario_local', COUNT(*) FROM usuario_local
UNION ALL SELECT 'rol_local', COUNT(*) FROM rol_local
UNION ALL SELECT 'permiso_local', COUNT(*) FROM permiso_local
UNION ALL SELECT 'rol_permiso_local', COUNT(*) FROM rol_permiso_local
UNION ALL SELECT 'usuario_rol_local', COUNT(*) FROM usuario_rol_local
UNION ALL SELECT 'auditoria_evento', COUNT(*) FROM auditoria_evento
UNION ALL SELECT 'tercero', COUNT(*) FROM tercero
UNION ALL SELECT 'cliente_perfil', COUNT(*) FROM cliente_perfil
UNION ALL SELECT 'proveedor_perfil', COUNT(*) FROM proveedor_perfil
UNION ALL SELECT 'tercero_contacto', COUNT(*) FROM tercero_contacto
UNION ALL SELECT 'tercero_direccion', COUNT(*) FROM tercero_direccion
UNION ALL SELECT 'categoria', COUNT(*) FROM categoria
UNION ALL SELECT 'marca', COUNT(*) FROM marca
UNION ALL SELECT 'unidad_medida', COUNT(*) FROM unidad_medida
UNION ALL SELECT 'proveedor', COUNT(*) FROM proveedor
UNION ALL SELECT 'producto', COUNT(*) FROM producto
UNION ALL SELECT 'lote_producto', COUNT(*) FROM lote_producto
UNION ALL SELECT 'compra', COUNT(*) FROM compra
UNION ALL SELECT 'detalle_compra', COUNT(*) FROM detalle_compra
UNION ALL SELECT 'cuenta_por_pagar', COUNT(*) FROM cuenta_por_pagar
UNION ALL SELECT 'pago_proveedor', COUNT(*) FROM pago_proveedor
UNION ALL SELECT 'venta_interna', COUNT(*) FROM venta_interna
UNION ALL SELECT 'detalle_venta_interna', COUNT(*) FROM detalle_venta_interna
UNION ALL SELECT 'venta_pago', COUNT(*) FROM venta_pago
UNION ALL SELECT 'anulacion_venta', COUNT(*) FROM anulacion_venta
UNION ALL SELECT 'movimiento_inventario', COUNT(*) FROM movimiento_inventario
UNION ALL SELECT 'tipo_movimiento_inventario', COUNT(*) FROM tipo_movimiento_inventario
UNION ALL SELECT 'conteo_inventario', COUNT(*) FROM conteo_inventario
UNION ALL SELECT 'conteo_inventario_detalle', COUNT(*) FROM conteo_inventario_detalle
UNION ALL SELECT 'ajuste_inventario', COUNT(*) FROM ajuste_inventario
UNION ALL SELECT 'ajuste_inventario_detalle', COUNT(*) FROM ajuste_inventario_detalle
UNION ALL SELECT 'merma_retiro', COUNT(*) FROM merma_retiro
UNION ALL SELECT 'caja_diaria', COUNT(*) FROM caja_diaria
UNION ALL SELECT 'movimiento_caja', COUNT(*) FROM movimiento_caja
UNION ALL SELECT 'cliente_fiado', COUNT(*) FROM cliente_fiado
UNION ALL SELECT 'cuenta_por_cobrar', COUNT(*) FROM cuenta_por_cobrar
UNION ALL SELECT 'abono', COUNT(*) FROM abono
UNION ALL SELECT 'tipo_identificacion_local', COUNT(*) FROM tipo_identificacion_local
UNION ALL SELECT 'tipo_comprobante_local', COUNT(*) FROM tipo_comprobante_local
UNION ALL SELECT 'impuesto_configuracion', COUNT(*) FROM impuesto_configuracion
UNION ALL SELECT 'documento_fiscal_preparado', COUNT(*) FROM documento_fiscal_preparado
UNION ALL SELECT 'documento_fiscal_preparado_detalle', COUNT(*) FROM documento_fiscal_preparado_detalle
UNION ALL SELECT 'licencia_sistema', COUNT(*) FROM licencia_sistema
UNION ALL SELECT 'respaldo_sistema', COUNT(*) FROM respaldo_sistema
UNION ALL SELECT 'ayuda_contextual', COUNT(*) FROM ayuda_contextual
UNION ALL SELECT 'parametro_configuracion', COUNT(*) FROM parametro_configuracion;

PRAGMA foreign_key_check;


-- Smoke check caja/gastos.
SELECT 'tipo_movimiento_caja' AS tabla, COUNT(*) AS total FROM tipo_movimiento_caja;
SELECT 'forma_pago_local' AS tabla, COUNT(*) AS total FROM forma_pago_local;
SELECT 'tipo_gasto' AS tabla, COUNT(*) AS total FROM tipo_gasto;
SELECT 'gasto_operativo' AS tabla, COUNT(*) AS total FROM gasto_operativo;
SELECT 'arqueo_caja' AS tabla, COUNT(*) AS total FROM arqueo_caja;


-- Smoke check cartera conectada con caja.
SELECT 'abono.movimiento_caja_id' AS columna, COUNT(*) AS existe FROM pragma_table_info('abono') WHERE name = 'movimiento_caja_id';
SELECT 'pago_proveedor.movimiento_caja_id' AS columna, COUNT(*) AS existe FROM pragma_table_info('pago_proveedor') WHERE name = 'movimiento_caja_id';
SELECT 'venta_pago.movimiento_caja_id' AS columna, COUNT(*) AS existe FROM pragma_table_info('venta_pago') WHERE name = 'movimiento_caja_id';


-- Smoke check fiscalidad preparada.
SELECT 'tipo_identificacion_local' AS tabla, COUNT(*) AS total FROM tipo_identificacion_local;
SELECT 'tipo_comprobante_local' AS tabla, COUNT(*) AS total FROM tipo_comprobante_local;
SELECT 'impuesto_configuracion' AS tabla, COUNT(*) AS total FROM impuesto_configuracion;
SELECT 'documento_fiscal_preparado' AS tabla, COUNT(*) AS total FROM documento_fiscal_preparado;
SELECT 'documento_fiscal_preparado_detalle' AS tabla, COUNT(*) AS total FROM documento_fiscal_preparado_detalle;


-- Smoke check contabilidad básica.
SELECT 'tipo_cuenta_contable' AS tabla, COUNT(*) AS total FROM tipo_cuenta_contable;
SELECT 'cuenta_contable' AS tabla, COUNT(*) AS total FROM cuenta_contable;
SELECT 'tipo_diario_contable' AS tabla, COUNT(*) AS total FROM tipo_diario_contable;
SELECT 'asiento_contable' AS tabla, COUNT(*) AS total FROM asiento_contable;
SELECT 'asiento_contable_detalle' AS tabla, COUNT(*) AS total FROM asiento_contable_detalle;
SELECT 'plantilla_asiento' AS tabla, COUNT(*) AS total FROM plantilla_asiento;
SELECT 'plantilla_asiento_detalle' AS tabla, COUNT(*) AS total FROM plantilla_asiento_detalle;
SELECT 'regla_contable_evento' AS tabla, COUNT(*) AS total FROM regla_contable_evento;


-- Smoke check opcionales mínimos.
SELECT 'tipo_activo_negocio' AS tabla, COUNT(*) AS total FROM tipo_activo_negocio;
SELECT 'activo_negocio' AS tabla, COUNT(*) AS total FROM activo_negocio;
SELECT 'cargo_empleado' AS tabla, COUNT(*) AS total FROM cargo_empleado;
SELECT 'empleado_local' AS tabla, COUNT(*) AS total FROM empleado_local;
SELECT 'indicador_operativo' AS tabla, COUNT(*) AS total FROM indicador_operativo;
SELECT 'consulta_reporte_log' AS tabla, COUNT(*) AS total FROM consulta_reporte_log;
SELECT 'plantilla_importacion' AS tabla, COUNT(*) AS total FROM plantilla_importacion;
SELECT 'lote_importacion' AS tabla, COUNT(*) AS total FROM lote_importacion;
SELECT 'error_importacion' AS tabla, COUNT(*) AS total FROM error_importacion;
SELECT 'checklist_operativo' AS tabla, COUNT(*) AS total FROM checklist_operativo;
SELECT 'checklist_item' AS tabla, COUNT(*) AS total FROM checklist_item;
