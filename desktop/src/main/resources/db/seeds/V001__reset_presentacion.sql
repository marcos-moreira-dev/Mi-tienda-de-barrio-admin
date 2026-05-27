-- Mi tienda de barrio admin
-- V001 - reset de datos inventados de presentación
-- No elimina catálogos mínimos ni configuración del cliente real.

PRAGMA foreign_keys = ON;

BEGIN TRANSACTION;

DELETE FROM abono
WHERE observacion LIKE '%presentación%';

DELETE FROM cuenta_por_cobrar
WHERE observacion LIKE '%presentación%';

DELETE FROM cliente_fiado
WHERE nombre IN ('Cliente Presentación Uno', 'Cliente Presentación Dos');

DELETE FROM movimiento_caja
WHERE descripcion LIKE '%presentación%'
   OR origen = 'VENTA_INTERNA' AND referencia_id IN (SELECT id FROM venta_interna WHERE numero_referencia LIKE 'PRES-%');

DELETE FROM caja_diaria
WHERE observacion LIKE '%presentación%';

DELETE FROM detalle_venta_interna
WHERE venta_interna_id IN (SELECT id FROM venta_interna WHERE numero_referencia LIKE 'PRES-%');

DELETE FROM venta_interna
WHERE numero_referencia LIKE 'PRES-%'
   OR observacion LIKE '%presentación%';

DELETE FROM detalle_compra
WHERE compra_id IN (SELECT id FROM compra WHERE numero_comprobante LIKE 'PRES-%');

DELETE FROM movimiento_inventario
WHERE observacion LIKE '%presentación%'
   OR referencia_tipo = 'COMPRA' AND referencia_id IN (SELECT id FROM compra WHERE numero_comprobante LIKE 'PRES-%');

DELETE FROM compra
WHERE numero_comprobante LIKE 'PRES-%'
   OR observacion LIKE '%presentación%';

DELETE FROM producto
WHERE codigo_interno LIKE 'PRES-%';

DELETE FROM proveedor
WHERE observacion LIKE '%presentación%'
  AND nombre IN ('Distribuidora Costa Víveres', 'Lácteos del Barrio', 'Mayorista Limpio Hogar');

DELETE FROM marca
WHERE descripcion LIKE '%presentación%'
  AND nombre IN ('Costa Demo', 'Barrio Fresco', 'Hogar Claro');

COMMIT;
