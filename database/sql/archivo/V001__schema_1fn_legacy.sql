-- Mi tienda de barrio admin
-- V001 - SQL 1FN legacy / schema bruto formal
--
-- OBJETIVO
-- Este archivo NO es el esquema oficial de ejecución final.
-- Sirve como corte formal de Primera Forma Normal (1FN) para mostrar el tránsito
-- desde una visión operativa más plana hacia la 3FN oficial.
--
-- REGLAS DEL ARCHIVO
-- - Todas las columnas son atómicas.
-- - No se guardan listas separadas por comas.
-- - Se permite repetir textos como categoria_nombre, marca_nombre o proveedor_nombre
--   porque este archivo representa una fase previa a la normalización completa.
-- - No se debe usar como migración oficial de la aplicación.
--
-- ESQUEMA OFICIAL FINAL:
-- database/sql/migrations/V001__schema_3fn_oficial.sql

PRAGMA foreign_keys = ON;

BEGIN TRANSACTION;

CREATE TABLE IF NOT EXISTS negocio_1fn (
    id INTEGER PRIMARY KEY CHECK (id = 1),
    nombre_comercial TEXT NOT NULL DEFAULT 'Mi tienda de barrio admin',
    ruc TEXT,
    responsable TEXT,
    telefono TEXT,
    direccion TEXT,
    actividad TEXT DEFAULT 'Tienda / despensa de barrio',
    moneda TEXT NOT NULL DEFAULT 'USD',
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS producto_1fn (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo_interno TEXT COLLATE NOCASE UNIQUE,
    nombre TEXT NOT NULL COLLATE NOCASE,
    descripcion TEXT,
    categoria_nombre TEXT NOT NULL COLLATE NOCASE,
    marca_nombre TEXT COLLATE NOCASE,
    unidad_nombre TEXT NOT NULL,
    unidad_abreviatura TEXT NOT NULL,
    unidad_permite_decimales INTEGER NOT NULL DEFAULT 0 CHECK (unidad_permite_decimales IN (0,1)),
    proveedor_principal_nombre TEXT COLLATE NOCASE,
    proveedor_principal_telefono TEXT,
    presentacion TEXT,
    precio_compra_referencia NUMERIC NOT NULL DEFAULT 0 CHECK (precio_compra_referencia >= 0),
    precio_venta NUMERIC NOT NULL DEFAULT 0 CHECK (precio_venta >= 0),
    stock_actual NUMERIC NOT NULL DEFAULT 0 CHECK (stock_actual >= 0),
    stock_minimo NUMERIC NOT NULL DEFAULT 0 CHECK (stock_minimo >= 0),
    stock_objetivo NUMERIC CHECK (stock_objetivo IS NULL OR stock_objetivo >= stock_minimo),
    maneja_lote INTEGER NOT NULL DEFAULT 0 CHECK (maneja_lote IN (0,1)),
    maneja_vencimiento INTEGER NOT NULL DEFAULT 0 CHECK (maneja_vencimiento IN (0,1)),
    perecible INTEGER NOT NULL DEFAULT 0 CHECK (perecible IN (0,1)),
    refrigerado INTEGER NOT NULL DEFAULT 0 CHECK (refrigerado IN (0,1)),
    ruta_foto TEXT,
    estado TEXT NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO','INACTIVO')),
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS recepcion_mercaderia_1fn (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha_recepcion TEXT NOT NULL DEFAULT (datetime('now')),
    proveedor_nombre TEXT COLLATE NOCASE,
    proveedor_telefono TEXT,
    numero_comprobante TEXT,
    tipo_comprobante TEXT NOT NULL DEFAULT 'SIN_COMPROBANTE' CHECK (tipo_comprobante IN ('SIN_COMPROBANTE','NOTA','FACTURA','OTRO')),
    producto_codigo_interno TEXT COLLATE NOCASE,
    producto_nombre TEXT NOT NULL COLLATE NOCASE,
    categoria_nombre TEXT,
    marca_nombre TEXT,
    unidad_abreviatura TEXT,
    cantidad NUMERIC NOT NULL CHECK (cantidad > 0),
    costo_unitario NUMERIC NOT NULL DEFAULT 0 CHECK (costo_unitario >= 0),
    subtotal NUMERIC NOT NULL DEFAULT 0 CHECK (subtotal >= 0),
    codigo_lote TEXT,
    fecha_vencimiento TEXT,
    producto_aceptado INTEGER NOT NULL DEFAULT 1 CHECK (producto_aceptado IN (0,1)),
    motivo_rechazo TEXT,
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS venta_interna_1fn (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha_venta TEXT NOT NULL DEFAULT (datetime('now')),
    producto_codigo_interno TEXT COLLATE NOCASE,
    producto_nombre TEXT NOT NULL COLLATE NOCASE,
    categoria_nombre TEXT,
    marca_nombre TEXT,
    unidad_abreviatura TEXT,
    lote_codigo TEXT,
    cantidad NUMERIC NOT NULL CHECK (cantidad > 0),
    precio_unitario NUMERIC NOT NULL DEFAULT 0 CHECK (precio_unitario >= 0),
    subtotal NUMERIC NOT NULL DEFAULT 0 CHECK (subtotal >= 0),
    metodo_pago TEXT NOT NULL DEFAULT 'EFECTIVO' CHECK (metodo_pago IN ('EFECTIVO','TRANSFERENCIA','OTRO','FIADO')),
    numero_referencia TEXT,
    advertencia_tributaria_aceptada INTEGER NOT NULL DEFAULT 0 CHECK (advertencia_tributaria_aceptada IN (0,1)),
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS movimiento_inventario_1fn (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha_movimiento TEXT NOT NULL DEFAULT (datetime('now')),
    producto_codigo_interno TEXT COLLATE NOCASE,
    producto_nombre TEXT NOT NULL COLLATE NOCASE,
    lote_codigo TEXT,
    tipo_movimiento TEXT NOT NULL CHECK (tipo_movimiento IN ('ENTRADA_COMPRA','SALIDA_VENTA_INTERNA','AJUSTE_POSITIVO','AJUSTE_NEGATIVO','MERMA','RETIRO_VENCIMIENTO','CORRECCION')),
    cantidad NUMERIC NOT NULL CHECK (cantidad > 0),
    stock_anterior NUMERIC NOT NULL CHECK (stock_anterior >= 0),
    stock_nuevo NUMERIC NOT NULL CHECK (stock_nuevo >= 0),
    referencia_tipo TEXT,
    referencia_id INTEGER,
    motivo TEXT,
    responsable_texto TEXT,
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS caja_diaria_1fn (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha TEXT NOT NULL,
    tipo_movimiento TEXT NOT NULL CHECK (tipo_movimiento IN ('APERTURA','INGRESO','EGRESO','CIERRE','AJUSTE')),
    origen TEXT,
    referencia_id INTEGER,
    monto NUMERIC NOT NULL DEFAULT 0 CHECK (monto >= 0),
    metodo_pago TEXT NOT NULL DEFAULT 'EFECTIVO' CHECK (metodo_pago IN ('EFECTIVO','TRANSFERENCIA','OTRO')),
    saldo_inicial NUMERIC NOT NULL DEFAULT 0 CHECK (saldo_inicial >= 0),
    saldo_esperado NUMERIC NOT NULL DEFAULT 0 CHECK (saldo_esperado >= 0),
    saldo_contado NUMERIC CHECK (saldo_contado IS NULL OR saldo_contado >= 0),
    diferencia NUMERIC NOT NULL DEFAULT 0,
    estado_caja TEXT NOT NULL DEFAULT 'ABIERTA' CHECK (estado_caja IN ('ABIERTA','CERRADA','ANULADA')),
    descripcion TEXT,
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS fiado_1fn (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    cliente_nombre TEXT NOT NULL COLLATE NOCASE,
    cliente_telefono TEXT,
    cliente_direccion TEXT,
    venta_interna_referencia_id INTEGER,
    fecha_evento TEXT NOT NULL DEFAULT (datetime('now')),
    tipo_evento TEXT NOT NULL CHECK (tipo_evento IN ('DEUDA','ABONO','ANULACION','AJUSTE')),
    monto_original NUMERIC NOT NULL DEFAULT 0 CHECK (monto_original >= 0),
    monto_abono NUMERIC NOT NULL DEFAULT 0 CHECK (monto_abono >= 0),
    saldo_pendiente NUMERIC NOT NULL DEFAULT 0 CHECK (saldo_pendiente >= 0),
    metodo_pago TEXT DEFAULT 'EFECTIVO' CHECK (metodo_pago IN ('EFECTIVO','TRANSFERENCIA','OTRO')),
    estado_cuenta TEXT NOT NULL DEFAULT 'ABIERTA' CHECK (estado_cuenta IN ('ABIERTA','CERRADA','ANULADA')),
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS sistema_local_1fn (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tipo_registro TEXT NOT NULL CHECK (tipo_registro IN ('LICENCIA','RESPALDO','PARAMETRO','AYUDA')),
    clave TEXT,
    titulo TEXT,
    valor TEXT,
    estado TEXT,
    fecha_inicio TEXT,
    fecha_fin TEXT,
    ruta_archivo TEXT,
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_producto_1fn_nombre ON producto_1fn(nombre);
CREATE INDEX IF NOT EXISTS idx_producto_1fn_categoria ON producto_1fn(categoria_nombre);
CREATE INDEX IF NOT EXISTS idx_recepcion_1fn_fecha ON recepcion_mercaderia_1fn(fecha_recepcion);
CREATE INDEX IF NOT EXISTS idx_venta_1fn_fecha ON venta_interna_1fn(fecha_venta);
CREATE INDEX IF NOT EXISTS idx_movimiento_1fn_producto_fecha ON movimiento_inventario_1fn(producto_codigo_interno, fecha_movimiento);
CREATE INDEX IF NOT EXISTS idx_fiado_1fn_cliente ON fiado_1fn(cliente_nombre, estado_cuenta);

COMMIT;
