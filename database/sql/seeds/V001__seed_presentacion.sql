-- Mi tienda de barrio admin
-- V001 - datos inventados para presentación comercial controlada
-- Ejecutar solo sobre una base de presentación o con respaldo previo.

PRAGMA foreign_keys = ON;

BEGIN TRANSACTION;

INSERT OR IGNORE INTO proveedor (nombre, telefono, whatsapp, direccion, observacion) VALUES
('Distribuidora Costa Víveres', '0991112223', '0991112223', 'Guayaquil', 'Proveedor ficticio para presentación.'),
('Lácteos del Barrio', '0983334445', '0983334445', 'Guayaquil', 'Proveedor ficticio para presentación.'),
('Mayorista Limpio Hogar', '0975556667', '0975556667', 'Durán', 'Proveedor ficticio para presentación.');

INSERT OR IGNORE INTO marca (nombre, descripcion) VALUES
('Costa Demo', 'Marca ficticia de presentación'),
('Barrio Fresco', 'Marca ficticia de presentación'),
('Hogar Claro', 'Marca ficticia de presentación');

INSERT OR IGNORE INTO producto (
    codigo_interno, nombre, descripcion, categoria_id, marca_id, unidad_medida_id, proveedor_principal_id,
    presentacion, precio_compra_referencia, precio_venta, stock_actual, stock_minimo, stock_objetivo,
    maneja_lote, maneja_vencimiento, perecible, refrigerado, observacion
) VALUES
('PRES-ARROZ-001', 'Arroz familiar 1 kg', 'Producto ficticio para mostrar inventario.',
 (SELECT id FROM categoria WHERE nombre = 'Víveres'), (SELECT id FROM marca WHERE nombre = 'Costa Demo'), (SELECT id FROM unidad_medida WHERE nombre = 'Kilogramo'), (SELECT id FROM proveedor WHERE nombre = 'Distribuidora Costa Víveres'),
 'Funda 1 kg', 0.72, 0.95, 32, 10, 40, 0, 0, 0, 0, 'Dato de presentación.'),
('PRES-ACEITE-001', 'Aceite vegetal 1 L', 'Producto ficticio para mostrar reposición.',
 (SELECT id FROM categoria WHERE nombre = 'Víveres'), (SELECT id FROM marca WHERE nombre = 'Costa Demo'), (SELECT id FROM unidad_medida WHERE nombre = 'Litro'), (SELECT id FROM proveedor WHERE nombre = 'Distribuidora Costa Víveres'),
 'Botella 1 L', 1.62, 2.10, 6, 8, 18, 0, 0, 0, 0, 'Bajo stock para captura.'),
('PRES-LECHE-001', 'Leche entera 1 L', 'Producto ficticio refrigerado.',
 (SELECT id FROM categoria WHERE nombre = 'Lácteos'), (SELECT id FROM marca WHERE nombre = 'Barrio Fresco'), (SELECT id FROM unidad_medida WHERE nombre = 'Litro'), (SELECT id FROM proveedor WHERE nombre = 'Lácteos del Barrio'),
 'Cartón 1 L', 0.83, 1.15, 14, 8, 24, 1, 1, 1, 1, 'Producto perecible de presentación.'),
('PRES-GALLETA-001', 'Galletas surtidas paquete', 'Producto ficticio para venta rápida.',
 (SELECT id FROM categoria WHERE nombre = 'Snacks'), (SELECT id FROM marca WHERE nombre = 'Costa Demo'), (SELECT id FROM unidad_medida WHERE nombre = 'Paquete'), (SELECT id FROM proveedor WHERE nombre = 'Distribuidora Costa Víveres'),
 'Paquete mediano', 0.35, 0.55, 48, 12, 60, 0, 0, 0, 0, 'Dato de presentación.'),
('PRES-DETERGENTE-001', 'Detergente en polvo 500 g', 'Producto ficticio de limpieza.',
 (SELECT id FROM categoria WHERE nombre = 'Limpieza'), (SELECT id FROM marca WHERE nombre = 'Hogar Claro'), (SELECT id FROM unidad_medida WHERE nombre = 'Gramo'), (SELECT id FROM proveedor WHERE nombre = 'Mayorista Limpio Hogar'),
 'Funda 500 g', 0.88, 1.25, 22, 7, 30, 0, 0, 0, 0, 'Dato de presentación.'),
('PRES-AGUA-001', 'Agua sin gas 600 ml', 'Producto ficticio de bebida.',
 (SELECT id FROM categoria WHERE nombre = 'Bebidas'), (SELECT id FROM marca WHERE nombre = 'Costa Demo'), (SELECT id FROM unidad_medida WHERE nombre = 'Botella'), (SELECT id FROM proveedor WHERE nombre = 'Distribuidora Costa Víveres'),
 'Botella 600 ml', 0.18, 0.35, 4, 12, 36, 0, 0, 0, 0, 'Bajo stock para captura.');

INSERT INTO compra (proveedor_id, fecha_compra, numero_comprobante, tipo_comprobante, total_estimado, observacion)
SELECT id, date('now', '-3 day'), 'PRES-COMPRA-001', 'NOTA', 78.40, 'Compra ficticia para presentación.'
FROM proveedor WHERE nombre = 'Distribuidora Costa Víveres'
AND NOT EXISTS (SELECT 1 FROM compra WHERE numero_comprobante = 'PRES-COMPRA-001');

INSERT INTO detalle_compra (compra_id, producto_id, cantidad, costo_unitario, subtotal, observacion)
SELECT c.id, p.id, 20, 0.72, 14.40, 'Detalle ficticio de presentación.'
FROM compra c, producto p
WHERE c.numero_comprobante = 'PRES-COMPRA-001' AND p.codigo_interno = 'PRES-ARROZ-001'
AND NOT EXISTS (SELECT 1 FROM detalle_compra WHERE compra_id = c.id AND producto_id = p.id);

INSERT INTO movimiento_inventario (producto_id, tipo_movimiento, cantidad, stock_anterior, stock_nuevo, referencia_tipo, referencia_id, motivo, responsable_texto, observacion)
SELECT p.id, 'ENTRADA_COMPRA', 20, 12, 32, 'COMPRA', c.id, 'Reposición de presentación', 'Administrador', 'Movimiento ficticio de presentación.'
FROM producto p, compra c
WHERE p.codigo_interno = 'PRES-ARROZ-001' AND c.numero_comprobante = 'PRES-COMPRA-001'
AND NOT EXISTS (SELECT 1 FROM movimiento_inventario WHERE producto_id = p.id AND referencia_tipo = 'COMPRA' AND referencia_id = c.id);

INSERT INTO venta_interna (fecha_venta, total, metodo_pago, numero_referencia, advertencia_tributaria_aceptada, observacion)
SELECT datetime('now', '-1 day'), 5.45, 'EFECTIVO', 'PRES-VENTA-001', 1, 'Venta interna ficticia para presentación.'
WHERE NOT EXISTS (SELECT 1 FROM venta_interna WHERE numero_referencia = 'PRES-VENTA-001');

INSERT INTO detalle_venta_interna (venta_interna_id, producto_id, cantidad, precio_unitario, subtotal, observacion)
SELECT v.id, p.id, 3, 0.95, 2.85, 'Detalle ficticio de presentación.'
FROM venta_interna v, producto p
WHERE v.numero_referencia = 'PRES-VENTA-001' AND p.codigo_interno = 'PRES-ARROZ-001'
AND NOT EXISTS (SELECT 1 FROM detalle_venta_interna WHERE venta_interna_id = v.id AND producto_id = p.id);

INSERT INTO detalle_venta_interna (venta_interna_id, producto_id, cantidad, precio_unitario, subtotal, observacion)
SELECT v.id, p.id, 2, 1.30, 2.60, 'Detalle ficticio de presentación.'
FROM venta_interna v, producto p
WHERE v.numero_referencia = 'PRES-VENTA-001' AND p.codigo_interno = 'PRES-DETERGENTE-001'
AND NOT EXISTS (SELECT 1 FROM detalle_venta_interna WHERE venta_interna_id = v.id AND producto_id = p.id);

INSERT INTO caja_diaria (fecha, saldo_inicial, total_ingresos, total_egresos, saldo_esperado, estado, observacion)
SELECT date('now'), 40.00, 18.50, 4.25, 54.25, 'ABIERTA', 'Caja ficticia abierta para presentación.'
WHERE NOT EXISTS (SELECT 1 FROM caja_diaria WHERE fecha = date('now'));

INSERT INTO movimiento_caja (caja_diaria_id, tipo_movimiento, origen, referencia_id, monto, metodo_pago, descripcion)
SELECT c.id, 'INGRESO', 'VENTA_INTERNA', v.id, 5.45, 'EFECTIVO', 'Ingreso ficticio por venta interna de presentación.'
FROM caja_diaria c, venta_interna v
WHERE c.fecha = date('now') AND v.numero_referencia = 'PRES-VENTA-001'
AND NOT EXISTS (SELECT 1 FROM movimiento_caja WHERE origen = 'VENTA_INTERNA' AND referencia_id = v.id);

INSERT OR IGNORE INTO cliente_fiado (nombre, telefono, direccion, limite_credito, observacion) VALUES
('Cliente Presentación Uno', '0990001111', 'Barrio de referencia', 30.00, 'Cliente ficticio para mostrar fiado.'),
('Cliente Presentación Dos', '0990002222', 'Sector cercano', 20.00, 'Cliente ficticio para mostrar cuentas por cobrar.');

INSERT INTO cuenta_por_cobrar (cliente_fiado_id, monto_original, saldo_pendiente, observacion)
SELECT id, 12.50, 7.50, 'Cuenta ficticia de presentación.'
FROM cliente_fiado WHERE nombre = 'Cliente Presentación Uno'
AND NOT EXISTS (SELECT 1 FROM cuenta_por_cobrar WHERE cliente_fiado_id = cliente_fiado.id AND observacion = 'Cuenta ficticia de presentación.');

INSERT INTO abono (cuenta_por_cobrar_id, monto, metodo_pago, observacion)
SELECT c.id, 5.00, 'EFECTIVO', 'Abono ficticio de presentación.'
FROM cuenta_por_cobrar c
JOIN cliente_fiado cf ON cf.id = c.cliente_fiado_id
WHERE cf.nombre = 'Cliente Presentación Uno'
AND NOT EXISTS (SELECT 1 FROM abono WHERE cuenta_por_cobrar_id = c.id AND observacion = 'Abono ficticio de presentación.');

COMMIT;
