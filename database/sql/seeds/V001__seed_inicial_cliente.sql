-- Mi tienda de barrio admin
-- V001 - seed inicial para cliente real limpio
-- No contiene productos, ventas, compras, caja ni clientes ficticios.

PRAGMA foreign_keys = ON;

BEGIN TRANSACTION;

INSERT OR IGNORE INTO schema_version (id, version, nombre_migracion, estado, observacion)
VALUES (1, 'V001', 'V001__schema_erp_local_sqlite_consolidado.sql', 'APLICADA', 'Base ERP local SQLite consolidada inicial.');


INSERT OR IGNORE INTO configuracion_negocio (id, nombre_comercial, actividad, moneda, observacion)
VALUES (1, 'Mi tienda de barrio admin', 'Tienda / despensa de barrio', 'USD', 'Configure aquí los datos reales del negocio antes de operar.');

INSERT OR IGNORE INTO licencia_sistema (id, estado, dias_gracia, modo_limitado, observacion)
VALUES (1, 'PENDIENTE', 7, 0, 'Licencia pendiente de activación. No bloquear datos del cliente.');


INSERT OR IGNORE INTO usuario_local (
    nombre_usuario, nombre_visible, password_hash, password_salt, algoritmo_hash,
    estado, debe_cambiar_clave, observacion
) VALUES (
    'admin',
    'Administrador local',
    '7e9246e4e6f5dfd6637260149dd320c406e45df5958625aec7436498c321c5ee',
    'mitienda-admin-local-v001',
    'SHA-256',
    'ACTIVO',
    1,
    'Usuario administrador inicial. Cambie esta contraseña antes de usar en un negocio real.'
);

INSERT OR IGNORE INTO rol_local (codigo, nombre, descripcion) VALUES
('ADMIN_LOCAL', 'Administrador local', 'Puede configurar y operar todos los módulos locales.'),
('OPERADOR_LOCAL', 'Operador local', 'Puede registrar operaciones diarias según permisos asignados.'),
('SOLO_LECTURA', 'Solo lectura', 'Puede consultar información y generar reportes sin registrar operaciones.');

INSERT OR IGNORE INTO permiso_local (codigo, nombre, modulo, descripcion) VALUES
('CONFIGURACION_LEER', 'Ver configuración', 'Configuración', 'Permite consultar datos del negocio.'),
('CONFIGURACION_EDITAR', 'Editar configuración', 'Configuración', 'Permite modificar datos del negocio.'),
('PRODUCTOS_OPERAR', 'Operar productos', 'Productos', 'Permite crear y editar productos y catálogos.'),
('COMPRAS_OPERAR', 'Registrar compras', 'Compras', 'Permite registrar entradas de mercadería.'),
('CUENTAS_PAGAR_OPERAR', 'Operar cuentas por pagar', 'Compras', 'Permite registrar compras a crédito y pagos a proveedores.'),
('VENTAS_OPERAR', 'Registrar ventas internas', 'Ventas internas', 'Permite registrar salidas internas no tributarias.'),
('CAJA_OPERAR', 'Operar caja', 'Caja', 'Permite abrir caja, registrar movimientos y cerrar caja.'),
('FIADO_OPERAR', 'Operar fiado', 'Fiado', 'Permite registrar cuentas por cobrar y abonos.'),
('REPORTES_LEER', 'Ver reportes', 'Reportes', 'Permite consultar y exportar reportes.'),
('RESPALDOS_OPERAR', 'Operar respaldos', 'Respaldos', 'Permite crear y restaurar respaldos.'),
('LICENCIA_OPERAR', 'Operar licencia', 'Licencia', 'Permite revisar y renovar licencia local.'),
('USUARIOS_OPERAR', 'Operar usuarios', 'Usuarios', 'Permite administrar usuarios, roles y permisos locales.'),
('TERCEROS_OPERAR', 'Operar clientes y proveedores', 'Clientes y proveedores', 'Permite registrar clientes, proveedores, contactos y direcciones.'),
('INVENTARIO_OPERAR', 'Operar inventario fuerte', 'Inventario', 'Permite registrar conteos físicos, ajustes formales y revisiones de stock.');

INSERT OR IGNORE INTO rol_permiso_local (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol_local r
CROSS JOIN permiso_local p
WHERE r.codigo = 'ADMIN_LOCAL';

INSERT OR IGNORE INTO rol_permiso_local (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol_local r
JOIN permiso_local p ON p.codigo IN (
    'PRODUCTOS_OPERAR', 'COMPRAS_OPERAR', 'CUENTAS_PAGAR_OPERAR', 'VENTAS_OPERAR', 'CAJA_OPERAR',
    'FIADO_OPERAR', 'REPORTES_LEER', 'RESPALDOS_OPERAR', 'TERCEROS_OPERAR', 'INVENTARIO_OPERAR'
)
WHERE r.codigo = 'OPERADOR_LOCAL';

INSERT OR IGNORE INTO rol_permiso_local (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol_local r
JOIN permiso_local p ON p.codigo IN ('CONFIGURACION_LEER', 'REPORTES_LEER')
WHERE r.codigo = 'SOLO_LECTURA';

INSERT OR IGNORE INTO usuario_rol_local (usuario_id, rol_id)
SELECT u.id, r.id
FROM usuario_local u
JOIN rol_local r ON r.codigo = 'ADMIN_LOCAL'
WHERE u.nombre_usuario = 'admin';


INSERT OR IGNORE INTO tercero (
    tipo_tercero, tipo_identificacion, numero_identificacion, nombre_legal,
    nombre_comercial, observacion
) VALUES
('CONSUMIDOR_FINAL', 'CONSUMIDOR_FINAL', '9999999999999', 'Consumidor final', 'Consumidor final local', 'Registro base para ventas internas sin cliente identificado.'),
('NEGOCIO', NULL, NULL, 'Proveedor no especificado', 'Proveedor no especificado', 'Registro base para compras sin proveedor identificado.');

INSERT OR IGNORE INTO cliente_perfil (tercero_id, permite_fiado, observacion)
SELECT id, 0, 'Cliente base para operaciones sin identificación individual.'
FROM tercero
WHERE nombre_comercial = 'Consumidor final local';

INSERT OR IGNORE INTO proveedor_perfil (tercero_id, observacion)
SELECT id, 'Proveedor base para operaciones sin dato registrado.'
FROM tercero
WHERE nombre_comercial = 'Proveedor no especificado';

INSERT OR IGNORE INTO categoria (nombre, descripcion) VALUES
('Víveres', 'Productos alimenticios generales'),
('Bebidas', 'Bebidas, jugos, gaseosas y agua'),
('Limpieza', 'Productos de limpieza del hogar'),
('Aseo personal', 'Productos de higiene personal'),
('Snacks', 'Galletas, dulces, piqueos y similares'),
('Lácteos', 'Leche, yogur, queso y derivados'),
('Carnes y refrigerados', 'Productos que pueden requerir refrigeración'),
('Papelería', 'Lápices, hojas, cuadernos y artículos escolares'),
('Bazar', 'Artículos varios de bazar'),
('Otros', 'Clasificación temporal o general');

INSERT OR IGNORE INTO marca (nombre, descripcion) VALUES
('Sin marca', 'Marca genérica para productos no diferenciados');

INSERT OR IGNORE INTO unidad_medida (nombre, abreviatura, permite_decimales) VALUES
('Unidad', 'u', 0),
('Caja', 'caja', 0),
('Paquete', 'paq', 0),
('Funda', 'funda', 0),
('Botella', 'bot', 0),
('Litro', 'L', 1),
('Kilogramo', 'kg', 1),
('Libra', 'lb', 1),
('Gramo', 'g', 1);

INSERT OR IGNORE INTO proveedor (nombre, observacion) VALUES
('Proveedor no especificado', 'Proveedor genérico para compras sin dato registrado');


INSERT OR IGNORE INTO tipo_movimiento_inventario (codigo, nombre, signo, afecta_stock, reservado_sistema) VALUES
('ENTRADA_COMPRA', 'Entrada por compra', 1, 1, 1),
('SALIDA_VENTA_INTERNA', 'Salida por venta interna', -1, 1, 1),
('AJUSTE_POSITIVO', 'Ajuste positivo', 1, 1, 0),
('AJUSTE_NEGATIVO', 'Ajuste negativo', -1, 1, 0),
('MERMA', 'Merma', -1, 1, 0),
('RETIRO_VENCIMIENTO', 'Retiro por vencimiento', -1, 1, 0),
('CORRECCION', 'Corrección de stock', 0, 1, 0);

INSERT OR IGNORE INTO parametro_configuracion (clave, valor, tipo_valor, descripcion) VALUES
('stock.permitir_negativo', 'false', 'BOOLEANO', 'En V001 se recomienda no permitir stock negativo'),
('reportes.dias_proximo_vencimiento', '15', 'NUMERO', 'Días para considerar un producto próximo a vencer'),
('respaldo.recordatorio_dias', '7', 'NUMERO', 'Días máximos sugeridos sin crear respaldo'),
('licencia.dias_gracia_default', '7', 'NUMERO', 'Días de gracia sugeridos al vencer licencia'),
('ui.modulo_caja_visible', 'true', 'BOOLEANO', 'Caja diaria visible pero opcional'),
('ui.modulo_fiado_visible', 'true', 'BOOLEANO', 'Fiado visible pero opcional');

INSERT OR IGNORE INTO ayuda_contextual (modulo, clave, titulo, contenido) VALUES
('inicio', 'modo_cliente_real', 'Base limpia para cliente real', 'El sistema inicia con catálogos mínimos, pero sin productos, ventas, compras ni movimientos ficticios.'),
('configuracion', 'datos_negocio', 'Datos del negocio', 'Configure nombre comercial, responsable, teléfono, dirección y observaciones antes de operar.'),
('productos', 'stock_minimo', 'Stock mínimo', 'Cantidad desde la cual el sistema avisa que un producto está bajo y debe revisarse.'),
('productos', 'stock_objetivo', 'Stock objetivo', 'Cantidad deseada después de reponer mercadería. Sirve para calcular productos por comprar.'),
('compras', 'recepcion', 'Registrar entrada de mercadería', 'Use compras para aumentar stock y guardar proveedor, costo y observaciones.'),
('ventas-internas', 'no_tributario', 'Venta interna no tributaria', 'Este registro ayuda a descontar stock, pero no reemplaza factura ni comprobante autorizado.'),
('caja', 'opcional', 'Caja diaria opcional', 'Use caja diaria si el negocio desea controlar ingresos, egresos y cierre de efectivo del día.'),
('fiado', 'opcional', 'Fiado opcional', 'Use fiado solo si el negocio permite cuentas por cobrar informales a clientes conocidos.'),
('reportes', 'productos_por_comprar', 'Productos por comprar', 'Reporte que cruza stock actual, stock mínimo y stock objetivo para sugerir reposición.'),
('respaldos', 'importancia', 'Respaldos', 'Haga respaldos periódicos y guárdelos fuera de la computadora principal.'),
('licencia', 'modo_limitado', 'Modo limitado', 'Si la licencia vence, los datos siguen disponibles y se permite consulta, exportación, reportes y respaldos.');


INSERT OR IGNORE INTO tipo_identificacion_local(codigo,nombre,descripcion) VALUES
('CEDULA','Cédula','Documento de identificación nacional.'),
('RUC','RUC','Registro Único de Contribuyentes.'),
('PASAPORTE','Pasaporte','Documento para personas extranjeras.'),
('CONSUMIDOR_FINAL','Consumidor final','Cliente sin identificación individual para operación interna.'),
('OTRO','Otro','Identificación no clasificada.');

INSERT OR IGNORE INTO tipo_comprobante_local(codigo,nombre,descripcion,requiere_tercero,advertencia_no_autorizado) VALUES
('VENTA_INTERNA','Venta interna','Documento operativo interno para registrar salidas y ventas internas no tributarias.',0,'Documento interno. No reemplaza comprobante autorizado por el SRI.'),
('NOTA_VENTA_PREPARADA','Nota de venta preparada','Documento local preparado para control interno. No tiene autorización electrónica.',0,'Documento preparado. No reemplaza nota de venta autorizada por el SRI.'),
('FACTURA_PREPARADA','Factura preparada','Documento local preparado con datos referenciales. No tiene autorización electrónica.',1,'Documento preparado. No reemplaza factura autorizada por el SRI.'),
('COMPRA_INTERNA','Compra interna','Documento operativo interno para compras y entradas de mercadería.',0,'Documento interno de compra. No reemplaza comprobante oficial.'),
('ABONO','Abono','Documento interno para registrar abonos de fiado.',0,'Documento interno de abono. No reemplaza comprobante autorizado.'),
('PAGO_PROVEEDOR','Pago a proveedor','Documento interno para registrar pagos a proveedores.',0,'Documento interno de pago. No reemplaza comprobante autorizado.');

INSERT OR IGNORE INTO impuesto_configuracion(codigo,nombre,porcentaje,aplica_ventas,aplica_compras,observacion) VALUES
('SIN_IMPUESTO','Sin impuesto',0,1,1,'Impuesto cero para documentos internos/preparados. Configure porcentajes reales solo después de validarlos.' );

INSERT OR IGNORE INTO permiso_local (codigo, nombre, modulo, descripcion) VALUES
('FISCALIDAD_PREPARADA_OPERAR', 'Operar fiscalidad preparada', 'Fiscalidad preparada', 'Permite configurar documentos preparados e impuestos locales no autorizados.');

INSERT OR IGNORE INTO rol_permiso_local (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol_local r
JOIN permiso_local p ON p.codigo = 'FISCALIDAD_PREPARADA_OPERAR'
WHERE r.codigo = 'ADMIN_LOCAL';

INSERT OR IGNORE INTO ayuda_contextual (modulo, clave, titulo, contenido) VALUES
('fiscalidad-preparada', 'no_sri_real', 'Fiscalidad preparada, no SRI real', 'Los documentos preparados son referencias internas. No reemplazan comprobantes autorizados por el SRI ni facturación electrónica.'),
('fiscalidad-preparada', 'impuestos_configurables', 'Impuestos configurables', 'Los porcentajes deben revisarse antes de operar. No se deben quemar valores tributarios en código.');

COMMIT;


-- Catálogos base para caja y gastos locales.
INSERT OR IGNORE INTO tipo_movimiento_caja(codigo,nombre,naturaleza,descripcion) VALUES
('VENTA_CONTADO','Venta pagada','INGRESO','Ingreso de caja por venta interna pagada.'),
('ABONO_FIADO','Abono de fiado','INGRESO','Ingreso por abono a cuenta por cobrar.'),
('INGRESO_MANUAL','Ingreso manual','INGRESO','Ingreso registrado manualmente.'),
('GASTO_OPERATIVO','Gasto operativo','EGRESO','Egreso por gasto diario del negocio.'),
('PAGO_PROVEEDOR','Pago a proveedor','EGRESO','Egreso por pago de cuenta por pagar.'),
('RETIRO_DUENO','Retiro del dueño','EGRESO','Retiro de efectivo del negocio.'),
('EGRESO_MANUAL','Egreso manual','EGRESO','Egreso registrado manualmente.'),
('AJUSTE_CAJA','Ajuste de caja','AJUSTE','Ajuste controlado por diferencia o corrección.');

INSERT OR IGNORE INTO forma_pago_local(codigo,nombre,requiere_referencia) VALUES
('EFECTIVO','Efectivo',0),
('TRANSFERENCIA','Transferencia',1),
('TARJETA','Tarjeta',1),
('OTRO','Otro',0);

INSERT OR IGNORE INTO tipo_gasto(nombre,descripcion) VALUES
('Agua','Pago de agua u otros servicios básicos.'),
('Luz','Pago de electricidad.'),
('Internet','Pago de internet o telefonía.'),
('Transporte','Movilización, entregas o transporte.'),
('Limpieza','Insumos o servicios de limpieza.'),
('Reparación','Arreglos menores del local o equipos.'),
('Retiro del dueño','Retiro de efectivo para uso del dueño.'),
('Otro','Gasto operativo no clasificado.');


-- Catálogos mínimos de contabilidad básica local.
INSERT OR IGNORE INTO tipo_cuenta_contable(codigo,nombre,naturaleza) VALUES
('ACTIVO','Activo','DEUDORA'),
('PASIVO','Pasivo','ACREEDORA'),
('PATRIMONIO','Patrimonio','ACREEDORA'),
('INGRESO','Ingreso','ACREEDORA'),
('GASTO','Gasto','DEUDORA'),
('COSTO','Costo','DEUDORA');

INSERT OR IGNORE INTO tipo_diario_contable(codigo,nombre,descripcion) VALUES
('GENERAL','Diario general','Asientos manuales o ajustes generales.'),
('VENTAS','Diario de ventas','Asientos relacionados con ventas internas.'),
('COMPRAS','Diario de compras','Asientos relacionados con compras.'),
('CAJA','Diario de caja','Asientos relacionados con ingresos y egresos de caja.'),
('CARTERA','Diario de cartera','Asientos relacionados con cuentas por cobrar y por pagar.'),
('AJUSTES','Diario de ajustes','Asientos de ajuste o corrección.');

INSERT OR IGNORE INTO cuenta_contable(codigo,nombre,tipo,imputable,observacion) VALUES
('1.1.01','Caja','ACTIVO',1,'Efectivo local del negocio.'),
('1.1.02','Cuentas por cobrar','ACTIVO',1,'Fiado o deuda pendiente de clientes.'),
('1.1.03','Inventario','ACTIVO',1,'Mercadería disponible para venta interna.'),
('2.1.01','Cuentas por pagar','PASIVO',1,'Deudas pendientes con proveedores.'),
('3.1.01','Capital del negocio','PATRIMONIO',1,'Cuenta patrimonial básica.'),
('4.1.01','Ingresos por ventas internas','INGRESO',1,'Ingresos operativos internos. No equivale a facturación autorizada.'),
('5.1.01','Gastos operativos','GASTO',1,'Gastos diarios del negocio.'),
('5.2.01','Pérdidas por merma','GASTO',1,'Pérdidas por vencimiento, daño o retiro.'),
('6.1.01','Costo de ventas','COSTO',1,'Costo estimado de mercadería vendida, si se activa la política contable correspondiente.');



-- Plantillas y reglas contables locales. Son sugerencias internas; no reemplazan revision contable profesional.
INSERT OR IGNORE INTO plantilla_asiento(codigo,nombre,descripcion) VALUES
('VENTA_PAGADA','Venta interna pagada','Registra caja contra ingresos por venta interna pagada.'),
('VENTA_FIADA','Venta interna fiada','Registra cuentas por cobrar contra ingresos por venta interna fiada.'),
('ABONO_FIADO','Abono de fiado','Registra caja contra cuentas por cobrar.'),
('COMPRA_PAGADA','Compra pagada','Registra inventario contra caja por compra pagada.'),
('COMPRA_CREDITO','Compra a credito','Registra inventario contra cuentas por pagar.'),
('PAGO_PROVEEDOR','Pago a proveedor','Registra cuentas por pagar contra caja.'),
('GASTO_OPERATIVO','Gasto operativo','Registra gasto operativo contra caja.'),
('MERMA_INVENTARIO','Merma de inventario','Registra perdida por merma contra inventario.'),
('CAPITAL_INICIAL','Capital inicial','Registra caja contra capital del negocio.'),
('AJUSTE_GENERAL','Ajuste general','Plantilla simple para ajustes contables manuales.' );

INSERT OR IGNORE INTO plantilla_asiento_detalle(plantilla_id, cuenta_id, linea, lado, descripcion)
SELECT p.id, c.id, 1, 'DEBE', 'Ingreso de efectivo en caja'
FROM plantilla_asiento p JOIN cuenta_contable c ON c.codigo = '1.1.01'
WHERE p.codigo IN ('VENTA_PAGADA','ABONO_FIADO','CAPITAL_INICIAL');
INSERT OR IGNORE INTO plantilla_asiento_detalle(plantilla_id, cuenta_id, linea, lado, descripcion)
SELECT p.id, c.id, 2, 'HABER', 'Reconocimiento de ingreso o contrapartida'
FROM plantilla_asiento p JOIN cuenta_contable c ON c.codigo = CASE p.codigo
    WHEN 'VENTA_PAGADA' THEN '4.1.01'
    WHEN 'ABONO_FIADO' THEN '1.1.02'
    WHEN 'CAPITAL_INICIAL' THEN '3.1.01'
END
WHERE p.codigo IN ('VENTA_PAGADA','ABONO_FIADO','CAPITAL_INICIAL');

INSERT OR IGNORE INTO plantilla_asiento_detalle(plantilla_id, cuenta_id, linea, lado, descripcion)
SELECT p.id, c.id, 1, 'DEBE', 'Reconocimiento de cuenta por cobrar'
FROM plantilla_asiento p JOIN cuenta_contable c ON c.codigo = '1.1.02'
WHERE p.codigo = 'VENTA_FIADA';
INSERT OR IGNORE INTO plantilla_asiento_detalle(plantilla_id, cuenta_id, linea, lado, descripcion)
SELECT p.id, c.id, 2, 'HABER', 'Ingreso por venta interna fiada'
FROM plantilla_asiento p JOIN cuenta_contable c ON c.codigo = '4.1.01'
WHERE p.codigo = 'VENTA_FIADA';

INSERT OR IGNORE INTO plantilla_asiento_detalle(plantilla_id, cuenta_id, linea, lado, descripcion)
SELECT p.id, c.id, 1, 'DEBE', 'Entrada de inventario'
FROM plantilla_asiento p JOIN cuenta_contable c ON c.codigo = '1.1.03'
WHERE p.codigo IN ('COMPRA_PAGADA','COMPRA_CREDITO');
INSERT OR IGNORE INTO plantilla_asiento_detalle(plantilla_id, cuenta_id, linea, lado, descripcion)
SELECT p.id, c.id, 2, 'HABER', 'Salida de caja o deuda con proveedor'
FROM plantilla_asiento p JOIN cuenta_contable c ON c.codigo = CASE p.codigo
    WHEN 'COMPRA_PAGADA' THEN '1.1.01'
    WHEN 'COMPRA_CREDITO' THEN '2.1.01'
END
WHERE p.codigo IN ('COMPRA_PAGADA','COMPRA_CREDITO');

INSERT OR IGNORE INTO plantilla_asiento_detalle(plantilla_id, cuenta_id, linea, lado, descripcion)
SELECT p.id, c.id, 1, 'DEBE', 'Reduccion de cuenta por pagar'
FROM plantilla_asiento p JOIN cuenta_contable c ON c.codigo = '2.1.01'
WHERE p.codigo = 'PAGO_PROVEEDOR';
INSERT OR IGNORE INTO plantilla_asiento_detalle(plantilla_id, cuenta_id, linea, lado, descripcion)
SELECT p.id, c.id, 2, 'HABER', 'Salida de caja'
FROM plantilla_asiento p JOIN cuenta_contable c ON c.codigo = '1.1.01'
WHERE p.codigo = 'PAGO_PROVEEDOR';

INSERT OR IGNORE INTO plantilla_asiento_detalle(plantilla_id, cuenta_id, linea, lado, descripcion)
SELECT p.id, c.id, 1, 'DEBE', 'Gasto operativo'
FROM plantilla_asiento p JOIN cuenta_contable c ON c.codigo = '5.1.01'
WHERE p.codigo = 'GASTO_OPERATIVO';
INSERT OR IGNORE INTO plantilla_asiento_detalle(plantilla_id, cuenta_id, linea, lado, descripcion)
SELECT p.id, c.id, 2, 'HABER', 'Salida de caja por gasto'
FROM plantilla_asiento p JOIN cuenta_contable c ON c.codigo = '1.1.01'
WHERE p.codigo = 'GASTO_OPERATIVO';

INSERT OR IGNORE INTO plantilla_asiento_detalle(plantilla_id, cuenta_id, linea, lado, descripcion)
SELECT p.id, c.id, 1, 'DEBE', 'Perdida por merma'
FROM plantilla_asiento p JOIN cuenta_contable c ON c.codigo = '5.2.01'
WHERE p.codigo = 'MERMA_INVENTARIO';
INSERT OR IGNORE INTO plantilla_asiento_detalle(plantilla_id, cuenta_id, linea, lado, descripcion)
SELECT p.id, c.id, 2, 'HABER', 'Salida de inventario por merma'
FROM plantilla_asiento p JOIN cuenta_contable c ON c.codigo = '1.1.03'
WHERE p.codigo = 'MERMA_INVENTARIO';

INSERT OR IGNORE INTO plantilla_asiento_detalle(plantilla_id, cuenta_id, linea, lado, descripcion)
SELECT p.id, c.id, 1, 'DEBE', 'Cuenta de ajuste'
FROM plantilla_asiento p JOIN cuenta_contable c ON c.codigo = '5.1.01'
WHERE p.codigo = 'AJUSTE_GENERAL';
INSERT OR IGNORE INTO plantilla_asiento_detalle(plantilla_id, cuenta_id, linea, lado, descripcion)
SELECT p.id, c.id, 2, 'HABER', 'Contrapartida de ajuste'
FROM plantilla_asiento p JOIN cuenta_contable c ON c.codigo = '1.1.01'
WHERE p.codigo = 'AJUSTE_GENERAL';

INSERT OR IGNORE INTO regla_contable_evento(evento_codigo, plantilla_id, descripcion)
SELECT 'VENTA_PAGADA', id, 'Sugerencia contable para venta interna pagada.' FROM plantilla_asiento WHERE codigo = 'VENTA_PAGADA';
INSERT OR IGNORE INTO regla_contable_evento(evento_codigo, plantilla_id, descripcion)
SELECT 'VENTA_FIADA', id, 'Sugerencia contable para venta interna fiada.' FROM plantilla_asiento WHERE codigo = 'VENTA_FIADA';
INSERT OR IGNORE INTO regla_contable_evento(evento_codigo, plantilla_id, descripcion)
SELECT 'ABONO_FIADO', id, 'Sugerencia contable para abono de fiado.' FROM plantilla_asiento WHERE codigo = 'ABONO_FIADO';
INSERT OR IGNORE INTO regla_contable_evento(evento_codigo, plantilla_id, descripcion)
SELECT 'COMPRA_PAGADA', id, 'Sugerencia contable para compra pagada.' FROM plantilla_asiento WHERE codigo = 'COMPRA_PAGADA';
INSERT OR IGNORE INTO regla_contable_evento(evento_codigo, plantilla_id, descripcion)
SELECT 'COMPRA_CREDITO', id, 'Sugerencia contable para compra a credito.' FROM plantilla_asiento WHERE codigo = 'COMPRA_CREDITO';
INSERT OR IGNORE INTO regla_contable_evento(evento_codigo, plantilla_id, descripcion)
SELECT 'PAGO_PROVEEDOR', id, 'Sugerencia contable para pago a proveedor.' FROM plantilla_asiento WHERE codigo = 'PAGO_PROVEEDOR';
INSERT OR IGNORE INTO regla_contable_evento(evento_codigo, plantilla_id, descripcion)
SELECT 'GASTO_OPERATIVO', id, 'Sugerencia contable para gasto operativo.' FROM plantilla_asiento WHERE codigo = 'GASTO_OPERATIVO';
INSERT OR IGNORE INTO regla_contable_evento(evento_codigo, plantilla_id, descripcion)
SELECT 'MERMA_INVENTARIO', id, 'Sugerencia contable para merma de inventario.' FROM plantilla_asiento WHERE codigo = 'MERMA_INVENTARIO';
INSERT OR IGNORE INTO regla_contable_evento(evento_codigo, plantilla_id, descripcion)
SELECT 'CAPITAL_INICIAL', id, 'Sugerencia contable para capital inicial.' FROM plantilla_asiento WHERE codigo = 'CAPITAL_INICIAL';
INSERT OR IGNORE INTO regla_contable_evento(evento_codigo, plantilla_id, descripcion)
SELECT 'AJUSTE_GENERAL', id, 'Sugerencia contable para ajuste general.' FROM plantilla_asiento WHERE codigo = 'AJUSTE_GENERAL';

INSERT OR IGNORE INTO permiso_local (codigo, nombre, modulo, descripcion) VALUES
('CONTABILIDAD_OPERAR', 'Operar contabilidad básica', 'Contabilidad básica', 'Permite registrar asientos contables internos y revisar el plan de cuentas.');

INSERT OR IGNORE INTO rol_permiso_local (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol_local r
JOIN permiso_local p ON p.codigo = 'CONTABILIDAD_OPERAR'
WHERE r.codigo = 'ADMIN_LOCAL';

INSERT OR IGNORE INTO ayuda_contextual (modulo, clave, titulo, contenido) VALUES
('contabilidad-basica', 'alcance', 'Contabilidad básica local', 'Este módulo ayuda a registrar asientos internos y revisar debe/haber. No reemplaza revisión profesional ni obligaciones contables o tributarias formales.'),
('contabilidad-basica', 'debe_haber', 'Debe y haber deben cuadrar', 'El sistema no registra un asiento si el total del debe y el total del haber son diferentes.');


-- Opcionales mínimos: activos, empleados, indicadores, importaciones y checklist operativo.
INSERT OR IGNORE INTO permiso_local (codigo, nombre, modulo, descripcion) VALUES
('OPCIONALES_OPERAR', 'Operar módulos opcionales mínimos', 'Opcionales mínimos', 'Permite registrar activos, empleados, importaciones y checklist operativo.');

INSERT OR IGNORE INTO rol_permiso_local (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol_local r
JOIN permiso_local p ON p.codigo = 'OPCIONALES_OPERAR'
WHERE r.codigo = 'ADMIN_LOCAL';

INSERT OR IGNORE INTO tipo_activo_negocio(codigo,nombre,descripcion) VALUES
('COMPUTADORA','Computadora','Equipo usado para operar el sistema.'),
('IMPRESORA','Impresora','Impresora de reportes, comprobantes o documentos internos.'),
('BALANZA','Balanza','Balanza para productos vendidos por peso.'),
('REFRIGERACION','Equipo de refrigeración','Refrigeradora, congelador o vitrina fría.'),
('MOBILIARIO','Mobiliario','Perchas, vitrinas, mesas o muebles del negocio.'),
('RED_ELECTRICA','Red y energía','Router, UPS, regulador u otros equipos de soporte.'),
('OTRO','Otro activo','Bien operativo no clasificado.');

INSERT OR IGNORE INTO cargo_empleado(codigo,nombre,descripcion) VALUES
('DUENO','Dueño / responsable','Responsable principal del negocio.'),
('CAJERO','Cajero','Persona encargada de caja y ventas.'),
('OPERADOR','Operador','Persona que registra operaciones diarias.'),
('AYUDANTE','Ayudante','Apoyo operativo del negocio.');

INSERT OR IGNORE INTO indicador_operativo(codigo,nombre,descripcion,modulo,orden_visual) VALUES
('PRODUCTOS_POR_COMPRAR','Productos por comprar','Productos bajo mínimo o agotados que requieren reposición.','Inventario',1),
('PROXIMOS_A_VENCER','Productos próximos a vencer','Productos con fecha de vencimiento cercana.','Inventario',2),
('CAJA_ABIERTA','Caja abierta','Indica si existe una caja diaria abierta.','Caja',3),
('FIADO_PENDIENTE','Fiado pendiente','Saldo pendiente de cuentas por cobrar.','Cartera',4),
('CUENTAS_POR_PAGAR','Cuentas por pagar','Deudas pendientes con proveedores.','Compras',5),
('ULTIMO_RESPALDO','Último respaldo','Fecha del último respaldo registrado.','Sistema',6);

INSERT OR IGNORE INTO plantilla_importacion(codigo,nombre,tipo_importacion,encabezados_csv,descripcion) VALUES
('PRODUCTOS_CSV','Productos desde CSV','PRODUCTOS','codigo,nombre,categoria,marca,unidad,precio_compra,precio_venta,stock_actual,stock_minimo','Plantilla básica para importar productos desde un archivo CSV.'),
('PROVEEDORES_CSV','Proveedores desde CSV','PROVEEDORES','nombre,telefono,whatsapp,direccion,observacion','Plantilla básica para importar proveedores.'),
('CLIENTES_FIADO_CSV','Clientes de fiado desde CSV','CLIENTES_FIADO','nombre,telefono,observacion','Plantilla básica para importar clientes de fiado.'),
('INVENTARIO_INICIAL_CSV','Inventario inicial desde CSV','INVENTARIO_INICIAL','codigo_producto,cantidad,costo_referencia','Plantilla básica para cargar inventario inicial.');

INSERT OR IGNORE INTO checklist_operativo(codigo,nombre,descripcion,frecuencia) VALUES
('RUTA_DIARIA','Ruta diaria de operación','Checklist diario para operar caja, ventas, fiado y respaldo.','DIARIA'),
('RUTA_SEMANAL','Ruta semanal de control','Checklist semanal para revisar inventario, vencimientos y cuentas pendientes.','SEMANAL');

INSERT OR IGNORE INTO checklist_item(checklist_id,orden,titulo,descripcion,modulo_relacionado)
SELECT c.id, 1, 'Abrir o revisar caja', 'Verifique que la caja diaria esté abierta antes de registrar ingresos o egresos.', 'Caja'
FROM checklist_operativo c WHERE c.codigo='RUTA_DIARIA';
INSERT OR IGNORE INTO checklist_item(checklist_id,orden,titulo,descripcion,modulo_relacionado)
SELECT c.id, 2, 'Registrar ventas y fiado', 'Registre ventas internas y marque como fiado solo cuando el cliente quede debiendo.', 'Ventas internas'
FROM checklist_operativo c WHERE c.codigo='RUTA_DIARIA';
INSERT OR IGNORE INTO checklist_item(checklist_id,orden,titulo,descripcion,modulo_relacionado)
SELECT c.id, 3, 'Cerrar caja', 'Al finalizar el día, revise saldo esperado, saldo contado y diferencias.', 'Caja'
FROM checklist_operativo c WHERE c.codigo='RUTA_DIARIA';
INSERT OR IGNORE INTO checklist_item(checklist_id,orden,titulo,descripcion,modulo_relacionado)
SELECT c.id, 4, 'Crear respaldo', 'Cree o verifique un respaldo reciente antes de cerrar la jornada.', 'Respaldos'
FROM checklist_operativo c WHERE c.codigo='RUTA_DIARIA';

INSERT OR IGNORE INTO checklist_item(checklist_id,orden,titulo,descripcion,modulo_relacionado)
SELECT c.id, 1, 'Revisar productos por comprar', 'Revise productos bajo stock, agotados o próximos a reposición.', 'Inventario'
FROM checklist_operativo c WHERE c.codigo='RUTA_SEMANAL';
INSERT OR IGNORE INTO checklist_item(checklist_id,orden,titulo,descripcion,modulo_relacionado)
SELECT c.id, 2, 'Revisar vencimientos', 'Revise productos próximos a vencer o que deban retirarse.', 'Inventario'
FROM checklist_operativo c WHERE c.codigo='RUTA_SEMANAL';
INSERT OR IGNORE INTO checklist_item(checklist_id,orden,titulo,descripcion,modulo_relacionado)
SELECT c.id, 3, 'Revisar fiado pendiente', 'Revise clientes con saldo pendiente y abonos recientes.', 'Cartera'
FROM checklist_operativo c WHERE c.codigo='RUTA_SEMANAL';
INSERT OR IGNORE INTO checklist_item(checklist_id,orden,titulo,descripcion,modulo_relacionado)
SELECT c.id, 4, 'Revisar cuentas por pagar', 'Revise compras a crédito y pagos pendientes a proveedores.', 'Compras'
FROM checklist_operativo c WHERE c.codigo='RUTA_SEMANAL';

INSERT OR IGNORE INTO ayuda_contextual (modulo, clave, titulo, contenido) VALUES
('opciones-minimas', 'alcance', 'Opcionales mínimos', 'Estos módulos preparan el sistema como ERP local sin convertirlo en nómina, BI empresarial o contabilidad de activos fijos formal.'),
('opciones-minimas', 'importacion_csv', 'Importación CSV controlada', 'La importación CSV debe validar encabezados y filas antes de insertar datos definitivos.'),
('opciones-minimas', 'checklist', 'Checklist operativo', 'Use el checklist como guía para rutinas diarias y semanales: caja, fiado, inventario, vencimientos y respaldos.');

-- Ayuda operativa ampliada: mini manual interno por rutas y módulos ERP locales.
INSERT OR IGNORE INTO ayuda_contextual (modulo, clave, titulo, contenido) VALUES
('inicio', 'ruta_diaria_operativa', 'Ruta diaria recomendada', '1) Abrir o revisar caja. 2) Registrar ventas internas y fiado cuando aplique. 3) Registrar compras o gastos del día. 4) Revisar productos por comprar. 5) Cerrar caja y crear respaldo.'),
('inicio', 'ruta_semanal_control', 'Ruta semanal de control', 'Revise productos próximos a vencer, productos agotados, fiado pendiente, cuentas por pagar, gastos de la semana y último respaldo. Esta revisión evita pérdidas pequeñas acumuladas.'),
('ventas-internas', 'venta_pagada_vs_fiada', 'Venta pagada y venta fiada', 'Una venta pagada debe conectarse con caja. Una venta fiada crea una cuenta por cobrar y no debe contarse como dinero recibido hasta registrar un abono.'),
('ventas-internas', 'anulacion_controlada', 'Anulación controlada', 'Si una venta fue registrada por error, no se debe borrar manualmente. Use anulación controlada para dejar evidencia y revertir el stock cuando corresponda.'),
('compras', 'compra_pagada_vs_credito', 'Compra pagada y compra a crédito', 'Una compra pagada representa salida de dinero. Una compra a crédito aumenta inventario y crea una cuenta por pagar, pero el dinero sale recién cuando se registre el pago al proveedor.'),
('caja', 'cierre_caja', 'Cierre de caja', 'Al cerrar caja compare saldo esperado y saldo contado. Si hay diferencia, deje observación. No fuerce los números para que cuadren sin explicación.'),
('caja', 'gastos_operativos', 'Gastos operativos', 'Registre gastos como agua, luz, transporte, limpieza, reparación o retiro del dueño. Así el reporte de caja no mezcla egresos sin explicación.'),
('fiado', 'abonos', 'Abonos de fiado', 'Cuando un cliente paga una parte de su deuda, registre un abono. El abono baja la cuenta por cobrar y debe entrar a caja si se recibió dinero real.'),
('cartera', 'cuentas_por_pagar', 'Cuentas por pagar', 'Las compras a crédito generan deudas con proveedores. Revise este módulo para saber qué debe el negocio y evitar olvidar pagos.'),
('inventario', 'conteo_ajuste', 'Conteo y ajuste de inventario', 'Use conteo para comparar stock físico contra el sistema. Use ajuste solo cuando haya una diferencia real y escriba el motivo.'),
('reportes', 'lectura_reportes', 'Cómo leer reportes', 'Use productos por comprar para reponer, fiado pendiente para cobrar, cuentas por pagar para planificar pagos y cierre de caja para revisar el día.'),
('respaldos', 'respaldo_seguro', 'Respaldo seguro', 'Cree respaldos frecuentes y guarde una copia fuera de la computadora principal. Antes de restaurar, el sistema debe validar que el archivo sea SQLite y esté íntegro.'),
('licencia', 'modo_limitado_etico', 'Modo limitado ético', 'Si la licencia está vencida o limitada, el sistema puede bloquear escrituras, pero debe permitir consultar, exportar reportes y crear respaldos.'),
('fiscalidad-preparada', 'documento_preparado', 'Documento preparado', 'Un documento preparado ayuda a ordenar información interna, pero no reemplaza comprobante autorizado por el SRI ni facturación electrónica real.'),
('contabilidad-basica', 'asiento_manual', 'Asiento manual', 'Registre asientos solo cuando entienda la operación. El sistema exige que debe y haber cuadren, pero eso no reemplaza revisión profesional.'),
('opciones-minimas', 'activos_empleados', 'Activos y empleados mínimos', 'Use activos para registrar bienes del negocio y empleados para trazabilidad operativa. No es depreciación formal ni nómina completa.'),
('opciones-minimas', 'importacion_segura', 'Importación segura', 'Antes de importar CSV, revise encabezados y errores. No inserte datos definitivos si el archivo tiene columnas mal escritas o valores inválidos.');

