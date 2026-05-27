-- Mi tienda de barrio admin
-- V001 - esquema SQLite 3FN oficial
-- Producto local JavaFX + SQLite para tiendas/despensas de barrio.

PRAGMA foreign_keys = ON;

BEGIN TRANSACTION;

CREATE TABLE IF NOT EXISTS configuracion_negocio (
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

CREATE TABLE IF NOT EXISTS categoria (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL COLLATE NOCASE UNIQUE,
    descripcion TEXT,
    estado TEXT NOT NULL DEFAULT 'ACTIVA' CHECK (estado IN ('ACTIVA','INACTIVA')),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS marca (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL COLLATE NOCASE UNIQUE,
    descripcion TEXT,
    estado TEXT NOT NULL DEFAULT 'ACTIVA' CHECK (estado IN ('ACTIVA','INACTIVA')),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS unidad_medida (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL COLLATE NOCASE UNIQUE,
    abreviatura TEXT NOT NULL,
    permite_decimales INTEGER NOT NULL DEFAULT 0 CHECK (permite_decimales IN (0,1)),
    estado TEXT NOT NULL DEFAULT 'ACTIVA' CHECK (estado IN ('ACTIVA','INACTIVA')),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS proveedor (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL COLLATE NOCASE,
    telefono TEXT,
    whatsapp TEXT,
    direccion TEXT,
    observacion TEXT,
    estado TEXT NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO','INACTIVO')),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    UNIQUE (nombre, telefono)
);

CREATE TABLE IF NOT EXISTS producto (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo_interno TEXT COLLATE NOCASE UNIQUE,
    nombre TEXT NOT NULL COLLATE NOCASE,
    descripcion TEXT,
    categoria_id INTEGER NOT NULL,
    marca_id INTEGER,
    unidad_medida_id INTEGER NOT NULL,
    proveedor_principal_id INTEGER,
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
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (categoria_id) REFERENCES categoria(id),
    FOREIGN KEY (marca_id) REFERENCES marca(id),
    FOREIGN KEY (unidad_medida_id) REFERENCES unidad_medida(id),
    FOREIGN KEY (proveedor_principal_id) REFERENCES proveedor(id)
);

CREATE TABLE IF NOT EXISTS lote_producto (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    producto_id INTEGER NOT NULL,
    proveedor_id INTEGER,
    codigo_lote TEXT,
    fecha_recepcion TEXT NOT NULL DEFAULT (date('now')),
    fecha_vencimiento TEXT,
    cantidad_inicial NUMERIC NOT NULL DEFAULT 0 CHECK (cantidad_inicial >= 0),
    cantidad_actual NUMERIC NOT NULL DEFAULT 0 CHECK (cantidad_actual >= 0),
    costo_unitario NUMERIC NOT NULL DEFAULT 0 CHECK (costo_unitario >= 0),
    estado TEXT NOT NULL DEFAULT 'DISPONIBLE' CHECK (estado IN ('DISPONIBLE','AGOTADO','VENCIDO','RETIRADO','ANULADO')),
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (producto_id) REFERENCES producto(id),
    FOREIGN KEY (proveedor_id) REFERENCES proveedor(id)
);

CREATE TABLE IF NOT EXISTS compra (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    proveedor_id INTEGER,
    fecha_compra TEXT NOT NULL DEFAULT (date('now')),
    numero_comprobante TEXT,
    tipo_comprobante TEXT NOT NULL DEFAULT 'SIN_COMPROBANTE' CHECK (tipo_comprobante IN ('SIN_COMPROBANTE','NOTA','FACTURA','OTRO')),
    total_estimado NUMERIC NOT NULL DEFAULT 0 CHECK (total_estimado >= 0),
    estado TEXT NOT NULL DEFAULT 'REGISTRADA' CHECK (estado IN ('REGISTRADA','ANULADA')),
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (proveedor_id) REFERENCES proveedor(id)
);

CREATE TABLE IF NOT EXISTS detalle_compra (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    compra_id INTEGER NOT NULL,
    producto_id INTEGER NOT NULL,
    lote_id INTEGER,
    cantidad NUMERIC NOT NULL CHECK (cantidad > 0),
    costo_unitario NUMERIC NOT NULL DEFAULT 0 CHECK (costo_unitario >= 0),
    subtotal NUMERIC NOT NULL DEFAULT 0 CHECK (subtotal >= 0),
    codigo_lote TEXT,
    fecha_vencimiento TEXT,
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (compra_id) REFERENCES compra(id) ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES producto(id),
    FOREIGN KEY (lote_id) REFERENCES lote_producto(id)
);

CREATE TABLE IF NOT EXISTS venta_interna (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha_venta TEXT NOT NULL DEFAULT (datetime('now')),
    total NUMERIC NOT NULL DEFAULT 0 CHECK (total >= 0),
    metodo_pago TEXT NOT NULL DEFAULT 'EFECTIVO' CHECK (metodo_pago IN ('EFECTIVO','TRANSFERENCIA','OTRO')),
    numero_referencia TEXT,
    estado TEXT NOT NULL DEFAULT 'REGISTRADA' CHECK (estado IN ('REGISTRADA','ANULADA')),
    advertencia_tributaria_aceptada INTEGER NOT NULL DEFAULT 0 CHECK (advertencia_tributaria_aceptada IN (0,1)),
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS detalle_venta_interna (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    venta_interna_id INTEGER NOT NULL,
    producto_id INTEGER NOT NULL,
    lote_id INTEGER,
    cantidad NUMERIC NOT NULL CHECK (cantidad > 0),
    precio_unitario NUMERIC NOT NULL DEFAULT 0 CHECK (precio_unitario >= 0),
    subtotal NUMERIC NOT NULL DEFAULT 0 CHECK (subtotal >= 0),
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (venta_interna_id) REFERENCES venta_interna(id) ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES producto(id),
    FOREIGN KEY (lote_id) REFERENCES lote_producto(id)
);

CREATE TABLE IF NOT EXISTS movimiento_inventario (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    producto_id INTEGER NOT NULL,
    lote_id INTEGER,
    tipo_movimiento TEXT NOT NULL CHECK (tipo_movimiento IN ('ENTRADA_COMPRA','SALIDA_VENTA_INTERNA','AJUSTE_POSITIVO','AJUSTE_NEGATIVO','MERMA','RETIRO_VENCIMIENTO','CORRECCION')),
    cantidad NUMERIC NOT NULL CHECK (cantidad > 0),
    stock_anterior NUMERIC NOT NULL CHECK (stock_anterior >= 0),
    stock_nuevo NUMERIC NOT NULL CHECK (stock_nuevo >= 0),
    fecha_movimiento TEXT NOT NULL DEFAULT (datetime('now')),
    referencia_tipo TEXT,
    referencia_id INTEGER,
    motivo TEXT,
    responsable_texto TEXT,
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (producto_id) REFERENCES producto(id),
    FOREIGN KEY (lote_id) REFERENCES lote_producto(id)
);

CREATE TABLE IF NOT EXISTS merma_retiro (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    producto_id INTEGER NOT NULL,
    lote_id INTEGER,
    tipo_motivo TEXT NOT NULL CHECK (tipo_motivo IN ('DANADO','VENCIDO','PERDIDA','CONSUMO_INTERNO','CORRECCION','OTRO')),
    cantidad NUMERIC NOT NULL CHECK (cantidad > 0),
    fecha_retiro TEXT NOT NULL DEFAULT (datetime('now')),
    stock_anterior NUMERIC NOT NULL CHECK (stock_anterior >= 0),
    stock_nuevo NUMERIC NOT NULL CHECK (stock_nuevo >= 0),
    ruta_foto_evidencia TEXT,
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (producto_id) REFERENCES producto(id),
    FOREIGN KEY (lote_id) REFERENCES lote_producto(id)
);

CREATE TABLE IF NOT EXISTS caja_diaria (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha TEXT NOT NULL UNIQUE,
    saldo_inicial NUMERIC NOT NULL DEFAULT 0 CHECK (saldo_inicial >= 0),
    total_ingresos NUMERIC NOT NULL DEFAULT 0 CHECK (total_ingresos >= 0),
    total_egresos NUMERIC NOT NULL DEFAULT 0 CHECK (total_egresos >= 0),
    saldo_esperado NUMERIC NOT NULL DEFAULT 0 CHECK (saldo_esperado >= 0),
    saldo_contado NUMERIC CHECK (saldo_contado IS NULL OR saldo_contado >= 0),
    diferencia NUMERIC NOT NULL DEFAULT 0,
    estado TEXT NOT NULL DEFAULT 'ABIERTA' CHECK (estado IN ('ABIERTA','CERRADA','ANULADA')),
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS movimiento_caja (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    caja_diaria_id INTEGER NOT NULL,
    tipo_movimiento TEXT NOT NULL CHECK (tipo_movimiento IN ('INGRESO','EGRESO','AJUSTE')),
    origen TEXT,
    referencia_id INTEGER,
    monto NUMERIC NOT NULL CHECK (monto > 0),
    metodo_pago TEXT NOT NULL DEFAULT 'EFECTIVO' CHECK (metodo_pago IN ('EFECTIVO','TRANSFERENCIA','OTRO')),
    descripcion TEXT NOT NULL,
    fecha_movimiento TEXT NOT NULL DEFAULT (datetime('now')),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (caja_diaria_id) REFERENCES caja_diaria(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS cliente_fiado (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL COLLATE NOCASE,
    telefono TEXT,
    direccion TEXT,
    limite_credito NUMERIC NOT NULL DEFAULT 0 CHECK (limite_credito >= 0),
    estado TEXT NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO','INACTIVO')),
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS cuenta_por_cobrar (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    cliente_fiado_id INTEGER NOT NULL,
    venta_interna_id INTEGER,
    fecha_apertura TEXT NOT NULL DEFAULT (datetime('now')),
    monto_original NUMERIC NOT NULL CHECK (monto_original > 0),
    saldo_pendiente NUMERIC NOT NULL CHECK (saldo_pendiente >= 0),
    estado TEXT NOT NULL DEFAULT 'ABIERTA' CHECK (estado IN ('ABIERTA','CERRADA','ANULADA')),
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (cliente_fiado_id) REFERENCES cliente_fiado(id),
    FOREIGN KEY (venta_interna_id) REFERENCES venta_interna(id)
);

CREATE TABLE IF NOT EXISTS abono (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    cuenta_por_cobrar_id INTEGER NOT NULL,
    fecha_abono TEXT NOT NULL DEFAULT (datetime('now')),
    monto NUMERIC NOT NULL CHECK (monto > 0),
    metodo_pago TEXT NOT NULL DEFAULT 'EFECTIVO' CHECK (metodo_pago IN ('EFECTIVO','TRANSFERENCIA','OTRO')),
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (cuenta_por_cobrar_id) REFERENCES cuenta_por_cobrar(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS licencia_sistema (
    id INTEGER PRIMARY KEY CHECK (id = 1),
    codigo_licencia TEXT,
    estado TEXT NOT NULL DEFAULT 'PENDIENTE' CHECK (estado IN ('PENDIENTE','ACTIVA','VENCIDA','MODO_LIMITADO','DESACTIVADA')),
    fecha_activacion TEXT,
    fecha_vencimiento TEXT,
    dias_gracia INTEGER NOT NULL DEFAULT 7 CHECK (dias_gracia >= 0),
    modo_limitado INTEGER NOT NULL DEFAULT 0 CHECK (modo_limitado IN (0,1)),
    ultima_validacion TEXT,
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS respaldo_sistema (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha_respaldo TEXT NOT NULL DEFAULT (datetime('now')),
    ruta_archivo TEXT NOT NULL,
    tipo_respaldo TEXT NOT NULL DEFAULT 'MANUAL' CHECK (tipo_respaldo IN ('MANUAL','AUTOMATICO','PRE_RESTAURACION','EXPORTACION')),
    estado TEXT NOT NULL DEFAULT 'CREADO' CHECK (estado IN ('CREADO','ERROR','RESTAURADO')),
    peso_bytes INTEGER CHECK (peso_bytes IS NULL OR peso_bytes >= 0),
    hash_sha256 TEXT,
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS ayuda_contextual (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    modulo TEXT NOT NULL,
    clave TEXT NOT NULL,
    titulo TEXT NOT NULL,
    contenido TEXT NOT NULL,
    estado TEXT NOT NULL DEFAULT 'ACTIVA' CHECK (estado IN ('ACTIVA','INACTIVA')),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    UNIQUE (modulo, clave)
);

CREATE TABLE IF NOT EXISTS parametro_configuracion (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    clave TEXT NOT NULL UNIQUE,
    valor TEXT,
    tipo_valor TEXT NOT NULL DEFAULT 'TEXTO' CHECK (tipo_valor IN ('TEXTO','NUMERO','BOOLEANO','RUTA','FECHA')),
    descripcion TEXT,
    editable INTEGER NOT NULL DEFAULT 1 CHECK (editable IN (0,1)),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_producto_nombre ON producto(nombre);
CREATE INDEX IF NOT EXISTS idx_producto_categoria ON producto(categoria_id);
CREATE INDEX IF NOT EXISTS idx_producto_estado ON producto(estado);
CREATE INDEX IF NOT EXISTS idx_producto_stock_bajo ON producto(stock_actual, stock_minimo);
CREATE INDEX IF NOT EXISTS idx_lote_producto_vencimiento ON lote_producto(fecha_vencimiento);
CREATE INDEX IF NOT EXISTS idx_compra_fecha ON compra(fecha_compra);
CREATE INDEX IF NOT EXISTS idx_movimiento_producto_fecha ON movimiento_inventario(producto_id, fecha_movimiento);
CREATE INDEX IF NOT EXISTS idx_venta_fecha ON venta_interna(fecha_venta);
CREATE INDEX IF NOT EXISTS idx_merma_fecha ON merma_retiro(fecha_retiro);
CREATE INDEX IF NOT EXISTS idx_cuenta_cliente_estado ON cuenta_por_cobrar(cliente_fiado_id, estado);

COMMIT;
