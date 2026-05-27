-- Mi tienda de barrio admin
-- V001 - esquema SQLite ERP local consolidado
-- Producto local JavaFX + SQLite para tiendas/despensas de barrio.
-- Base consolidada porque el proyecto aún no ha sido usado por clientes reales.

PRAGMA foreign_keys = ON;

BEGIN TRANSACTION;

CREATE TABLE IF NOT EXISTS schema_version (
    id INTEGER PRIMARY KEY CHECK (id = 1),
    version TEXT NOT NULL,
    nombre_migracion TEXT NOT NULL,
    fecha_aplicacion TEXT NOT NULL DEFAULT (datetime('now')),
    estado TEXT NOT NULL DEFAULT 'APLICADA' CHECK (estado IN ('APLICADA','ERROR','PENDIENTE')),
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

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


CREATE TABLE IF NOT EXISTS usuario_local (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_usuario TEXT NOT NULL COLLATE NOCASE UNIQUE,
    nombre_visible TEXT NOT NULL,
    password_hash TEXT NOT NULL,
    password_salt TEXT NOT NULL,
    algoritmo_hash TEXT NOT NULL DEFAULT 'SHA-256',
    estado TEXT NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO','INACTIVO','BLOQUEADO')),
    debe_cambiar_clave INTEGER NOT NULL DEFAULT 1 CHECK (debe_cambiar_clave IN (0,1)),
    ultimo_acceso TEXT,
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS rol_local (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo TEXT NOT NULL COLLATE NOCASE UNIQUE,
    nombre TEXT NOT NULL,
    descripcion TEXT,
    estado TEXT NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO','INACTIVO')),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS permiso_local (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo TEXT NOT NULL COLLATE NOCASE UNIQUE,
    nombre TEXT NOT NULL,
    modulo TEXT NOT NULL,
    descripcion TEXT,
    estado TEXT NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO','INACTIVO')),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS rol_permiso_local (
    rol_id INTEGER NOT NULL,
    permiso_id INTEGER NOT NULL,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    PRIMARY KEY (rol_id, permiso_id),
    FOREIGN KEY (rol_id) REFERENCES rol_local(id) ON DELETE CASCADE,
    FOREIGN KEY (permiso_id) REFERENCES permiso_local(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS usuario_rol_local (
    usuario_id INTEGER NOT NULL,
    rol_id INTEGER NOT NULL,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    PRIMARY KEY (usuario_id, rol_id),
    FOREIGN KEY (usuario_id) REFERENCES usuario_local(id) ON DELETE CASCADE,
    FOREIGN KEY (rol_id) REFERENCES rol_local(id) ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS auditoria_evento (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario_id INTEGER,
    fecha_evento TEXT NOT NULL DEFAULT (datetime('now')),
    modulo TEXT NOT NULL,
    accion TEXT NOT NULL,
    entidad TEXT,
    entidad_id INTEGER,
    resumen TEXT NOT NULL,
    detalle_json TEXT,
    resultado TEXT NOT NULL DEFAULT 'OK' CHECK (resultado IN ('OK','ADVERTENCIA','ERROR')),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (usuario_id) REFERENCES usuario_local(id) ON DELETE SET NULL
);


CREATE TABLE IF NOT EXISTS tercero (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tipo_tercero TEXT NOT NULL DEFAULT 'PERSONA_NATURAL' CHECK (tipo_tercero IN ('PERSONA_NATURAL','NEGOCIO','CONSUMIDOR_FINAL','OTRO')),
    tipo_identificacion TEXT CHECK (tipo_identificacion IS NULL OR tipo_identificacion IN ('CEDULA','RUC','PASAPORTE','CONSUMIDOR_FINAL','OTRO')),
    numero_identificacion TEXT,
    nombre_legal TEXT,
    nombre_comercial TEXT,
    telefono TEXT,
    whatsapp TEXT,
    correo TEXT,
    observacion TEXT,
    estado TEXT NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO','INACTIVO')),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    CHECK ((nombre_legal IS NOT NULL AND length(trim(nombre_legal)) > 0) OR (nombre_comercial IS NOT NULL AND length(trim(nombre_comercial)) > 0)),
    UNIQUE (tipo_identificacion, numero_identificacion)
);

CREATE TABLE IF NOT EXISTS cliente_perfil (
    tercero_id INTEGER PRIMARY KEY,
    permite_fiado INTEGER NOT NULL DEFAULT 0 CHECK (permite_fiado IN (0,1)),
    limite_credito NUMERIC NOT NULL DEFAULT 0 CHECK (limite_credito >= 0),
    dias_credito INTEGER NOT NULL DEFAULT 0 CHECK (dias_credito >= 0),
    observacion TEXT,
    estado TEXT NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO','INACTIVO')),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (tercero_id) REFERENCES tercero(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS proveedor_perfil (
    tercero_id INTEGER PRIMARY KEY,
    dias_credito INTEGER NOT NULL DEFAULT 0 CHECK (dias_credito >= 0),
    contacto_compras TEXT,
    observacion TEXT,
    estado TEXT NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO','INACTIVO')),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (tercero_id) REFERENCES tercero(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tercero_contacto (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tercero_id INTEGER NOT NULL,
    nombre TEXT NOT NULL,
    cargo TEXT,
    telefono TEXT,
    whatsapp TEXT,
    correo TEXT,
    principal INTEGER NOT NULL DEFAULT 0 CHECK (principal IN (0,1)),
    observacion TEXT,
    estado TEXT NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO','INACTIVO')),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (tercero_id) REFERENCES tercero(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tercero_direccion (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tercero_id INTEGER NOT NULL,
    tipo_direccion TEXT NOT NULL DEFAULT 'PRINCIPAL' CHECK (tipo_direccion IN ('PRINCIPAL','ENTREGA','FACTURACION','OTRA')),
    direccion TEXT NOT NULL,
    referencia TEXT,
    principal INTEGER NOT NULL DEFAULT 0 CHECK (principal IN (0,1)),
    estado TEXT NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO','INACTIVO')),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (tercero_id) REFERENCES tercero(id) ON DELETE CASCADE
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

CREATE TABLE IF NOT EXISTS cuenta_por_pagar (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    compra_id INTEGER NOT NULL UNIQUE,
    proveedor_id INTEGER,
    fecha_emision TEXT NOT NULL DEFAULT (date('now')),
    fecha_vencimiento TEXT,
    monto_total NUMERIC NOT NULL CHECK (monto_total >= 0),
    saldo_pendiente NUMERIC NOT NULL CHECK (saldo_pendiente >= 0),
    estado TEXT NOT NULL DEFAULT 'PENDIENTE' CHECK (estado IN ('PENDIENTE','PARCIAL','PAGADA','ANULADA')),
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (compra_id) REFERENCES compra(id) ON DELETE CASCADE,
    FOREIGN KEY (proveedor_id) REFERENCES proveedor(id)
);

CREATE TABLE IF NOT EXISTS pago_proveedor (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    cuenta_por_pagar_id INTEGER NOT NULL,
    movimiento_caja_id INTEGER,
    fecha_pago TEXT NOT NULL DEFAULT (date('now')),
    monto NUMERIC NOT NULL CHECK (monto > 0),
    forma_pago TEXT NOT NULL DEFAULT 'EFECTIVO' CHECK (forma_pago IN ('EFECTIVO','TRANSFERENCIA','TARJETA','OTRO')),
    referencia TEXT,
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (cuenta_por_pagar_id) REFERENCES cuenta_por_pagar(id) ON DELETE CASCADE,
    FOREIGN KEY (movimiento_caja_id) REFERENCES movimiento_caja(id)
);


CREATE TABLE IF NOT EXISTS venta_interna (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha_venta TEXT NOT NULL DEFAULT (datetime('now')),
    cliente_fiado_id INTEGER,
    total NUMERIC NOT NULL DEFAULT 0 CHECK (total >= 0),
    metodo_pago TEXT NOT NULL DEFAULT 'EFECTIVO' CHECK (metodo_pago IN ('EFECTIVO','TRANSFERENCIA','FIADO','OTRO')),
    numero_referencia TEXT,
    estado TEXT NOT NULL DEFAULT 'REGISTRADA' CHECK (estado IN ('REGISTRADA','ANULADA')),
    advertencia_tributaria_aceptada INTEGER NOT NULL DEFAULT 0 CHECK (advertencia_tributaria_aceptada IN (0,1)),
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (cliente_fiado_id) REFERENCES cliente_fiado(id)
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

CREATE TABLE IF NOT EXISTS venta_pago (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    venta_interna_id INTEGER NOT NULL,
    movimiento_caja_id INTEGER,
    fecha_pago TEXT NOT NULL DEFAULT (datetime('now')),
    monto NUMERIC NOT NULL CHECK (monto > 0),
    metodo_pago TEXT NOT NULL DEFAULT 'EFECTIVO' CHECK (metodo_pago IN ('EFECTIVO','TRANSFERENCIA','TARJETA','OTRO')),
    referencia TEXT,
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (venta_interna_id) REFERENCES venta_interna(id) ON DELETE CASCADE,
    FOREIGN KEY (movimiento_caja_id) REFERENCES movimiento_caja(id)
);

CREATE TABLE IF NOT EXISTS anulacion_venta (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    venta_interna_id INTEGER NOT NULL UNIQUE,
    fecha_anulacion TEXT NOT NULL DEFAULT (datetime('now')),
    motivo TEXT NOT NULL,
    responsable_texto TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (venta_interna_id) REFERENCES venta_interna(id) ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS tipo_movimiento_inventario (
    codigo TEXT PRIMARY KEY,
    nombre TEXT NOT NULL,
    signo INTEGER NOT NULL CHECK (signo IN (-1,0,1)),
    afecta_stock INTEGER NOT NULL DEFAULT 1 CHECK (afecta_stock IN (0,1)),
    reservado_sistema INTEGER NOT NULL DEFAULT 0 CHECK (reservado_sistema IN (0,1)),
    estado TEXT NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO','INACTIVO')),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS conteo_inventario (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha_conteo TEXT NOT NULL DEFAULT (datetime('now')),
    estado TEXT NOT NULL DEFAULT 'ABIERTO' CHECK (estado IN ('ABIERTO','CERRADO','ANULADO')),
    responsable_texto TEXT,
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS conteo_inventario_detalle (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    conteo_id INTEGER NOT NULL,
    producto_id INTEGER NOT NULL,
    stock_sistema NUMERIC NOT NULL DEFAULT 0 CHECK (stock_sistema >= 0),
    stock_contado NUMERIC NOT NULL DEFAULT 0 CHECK (stock_contado >= 0),
    diferencia NUMERIC NOT NULL DEFAULT 0,
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    UNIQUE (conteo_id, producto_id),
    FOREIGN KEY (conteo_id) REFERENCES conteo_inventario(id) ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES producto(id)
);

CREATE TABLE IF NOT EXISTS ajuste_inventario (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha_ajuste TEXT NOT NULL DEFAULT (datetime('now')),
    conteo_inventario_id INTEGER,
    estado TEXT NOT NULL DEFAULT 'REGISTRADO' CHECK (estado IN ('REGISTRADO','ANULADO')),
    responsable_texto TEXT,
    motivo TEXT NOT NULL,
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (conteo_inventario_id) REFERENCES conteo_inventario(id)
);

CREATE TABLE IF NOT EXISTS ajuste_inventario_detalle (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ajuste_inventario_id INTEGER NOT NULL,
    producto_id INTEGER NOT NULL,
    tipo_movimiento TEXT NOT NULL,
    cantidad NUMERIC NOT NULL CHECK (cantidad > 0),
    stock_anterior NUMERIC NOT NULL CHECK (stock_anterior >= 0),
    stock_nuevo NUMERIC NOT NULL CHECK (stock_nuevo >= 0),
    movimiento_inventario_id INTEGER,
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (ajuste_inventario_id) REFERENCES ajuste_inventario(id) ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES producto(id),
    FOREIGN KEY (tipo_movimiento) REFERENCES tipo_movimiento_inventario(codigo),
    FOREIGN KEY (movimiento_inventario_id) REFERENCES movimiento_inventario(id)
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


CREATE TABLE IF NOT EXISTS tipo_movimiento_caja (
    codigo TEXT PRIMARY KEY,
    nombre TEXT NOT NULL,
    naturaleza TEXT NOT NULL CHECK (naturaleza IN ('INGRESO','EGRESO','AJUSTE')),
    descripcion TEXT,
    activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0,1)),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS forma_pago_local (
    codigo TEXT PRIMARY KEY,
    nombre TEXT NOT NULL,
    requiere_referencia INTEGER NOT NULL DEFAULT 0 CHECK (requiere_referencia IN (0,1)),
    activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0,1)),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
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
    metodo_pago TEXT NOT NULL DEFAULT 'EFECTIVO' CHECK (metodo_pago IN ('EFECTIVO','TRANSFERENCIA','TARJETA','OTRO')),
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
    movimiento_caja_id INTEGER,
    fecha_abono TEXT NOT NULL DEFAULT (datetime('now')),
    monto NUMERIC NOT NULL CHECK (monto > 0),
    metodo_pago TEXT NOT NULL DEFAULT 'EFECTIVO' CHECK (metodo_pago IN ('EFECTIVO','TRANSFERENCIA','TARJETA','OTRO')),
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (cuenta_por_cobrar_id) REFERENCES cuenta_por_cobrar(id) ON DELETE CASCADE,
    FOREIGN KEY (movimiento_caja_id) REFERENCES movimiento_caja(id)
);


CREATE TABLE IF NOT EXISTS tipo_gasto (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL COLLATE NOCASE,
    descripcion TEXT,
    activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0,1)),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    UNIQUE(nombre)
);

CREATE TABLE IF NOT EXISTS gasto_operativo (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    caja_diaria_id INTEGER NOT NULL,
    tipo_gasto_id INTEGER NOT NULL,
    movimiento_caja_id INTEGER,
    fecha_gasto TEXT NOT NULL DEFAULT (datetime('now')),
    monto NUMERIC NOT NULL CHECK (monto > 0),
    forma_pago_codigo TEXT NOT NULL DEFAULT 'EFECTIVO',
    descripcion TEXT NOT NULL,
    referencia TEXT,
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (caja_diaria_id) REFERENCES caja_diaria(id) ON DELETE CASCADE,
    FOREIGN KEY (tipo_gasto_id) REFERENCES tipo_gasto(id),
    FOREIGN KEY (movimiento_caja_id) REFERENCES movimiento_caja(id),
    FOREIGN KEY (forma_pago_codigo) REFERENCES forma_pago_local(codigo)
);

CREATE TABLE IF NOT EXISTS arqueo_caja (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    caja_diaria_id INTEGER NOT NULL,
    fecha_arqueo TEXT NOT NULL DEFAULT (datetime('now')),
    saldo_sistema NUMERIC NOT NULL CHECK (saldo_sistema >= 0),
    saldo_contado NUMERIC NOT NULL CHECK (saldo_contado >= 0),
    diferencia NUMERIC NOT NULL,
    responsable_texto TEXT,
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (caja_diaria_id) REFERENCES caja_diaria(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tipo_identificacion_local (
    codigo TEXT PRIMARY KEY,
    nombre TEXT NOT NULL,
    descripcion TEXT,
    estado TEXT NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO','INACTIVO')),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS tipo_comprobante_local (
    codigo TEXT PRIMARY KEY,
    nombre TEXT NOT NULL,
    descripcion TEXT,
    requiere_tercero INTEGER NOT NULL DEFAULT 0 CHECK (requiere_tercero IN (0,1)),
    advertencia_no_autorizado TEXT NOT NULL DEFAULT 'Documento interno/preparado. No reemplaza comprobante autorizado por el SRI.',
    estado TEXT NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO','INACTIVO')),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS impuesto_configuracion (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo TEXT NOT NULL COLLATE NOCASE UNIQUE,
    nombre TEXT NOT NULL,
    porcentaje NUMERIC NOT NULL DEFAULT 0 CHECK (porcentaje >= 0),
    fecha_inicio TEXT,
    fecha_fin TEXT,
    aplica_ventas INTEGER NOT NULL DEFAULT 1 CHECK (aplica_ventas IN (0,1)),
    aplica_compras INTEGER NOT NULL DEFAULT 1 CHECK (aplica_compras IN (0,1)),
    estado TEXT NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO','INACTIVO')),
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS documento_fiscal_preparado (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tipo_comprobante_codigo TEXT NOT NULL,
    tercero_id INTEGER,
    venta_interna_id INTEGER,
    compra_id INTEGER,
    secuencia TEXT NOT NULL,
    fecha_emision TEXT NOT NULL DEFAULT (datetime('now')),
    estado TEXT NOT NULL DEFAULT 'PREPARADO' CHECK (estado IN ('BORRADOR','PREPARADO','ANULADO')),
    subtotal NUMERIC NOT NULL DEFAULT 0 CHECK (subtotal >= 0),
    impuesto_total NUMERIC NOT NULL DEFAULT 0 CHECK (impuesto_total >= 0),
    total NUMERIC NOT NULL DEFAULT 0 CHECK (total >= 0),
    advertencia_no_autorizado TEXT NOT NULL DEFAULT 'Documento interno/preparado. No reemplaza comprobante autorizado por el SRI.',
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (tipo_comprobante_codigo) REFERENCES tipo_comprobante_local(codigo),
    FOREIGN KEY (tercero_id) REFERENCES tercero(id),
    FOREIGN KEY (venta_interna_id) REFERENCES venta_interna(id),
    FOREIGN KEY (compra_id) REFERENCES compra(id),
    CHECK (venta_interna_id IS NOT NULL OR compra_id IS NOT NULL OR tercero_id IS NOT NULL)
);

CREATE TABLE IF NOT EXISTS documento_fiscal_preparado_detalle (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    documento_id INTEGER NOT NULL,
    producto_id INTEGER,
    descripcion TEXT NOT NULL,
    cantidad NUMERIC NOT NULL CHECK (cantidad > 0),
    precio_unitario NUMERIC NOT NULL DEFAULT 0 CHECK (precio_unitario >= 0),
    base_imponible NUMERIC NOT NULL DEFAULT 0 CHECK (base_imponible >= 0),
    impuesto_id INTEGER,
    valor_impuesto NUMERIC NOT NULL DEFAULT 0 CHECK (valor_impuesto >= 0),
    total_linea NUMERIC NOT NULL DEFAULT 0 CHECK (total_linea >= 0),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (documento_id) REFERENCES documento_fiscal_preparado(id) ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES producto(id),
    FOREIGN KEY (impuesto_id) REFERENCES impuesto_configuracion(id)
);



CREATE TABLE IF NOT EXISTS tipo_cuenta_contable (
    codigo TEXT PRIMARY KEY,
    nombre TEXT NOT NULL,
    naturaleza TEXT NOT NULL CHECK (naturaleza IN ('DEUDORA','ACREEDORA')),
    estado TEXT NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO','INACTIVO')),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS cuenta_contable (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo TEXT NOT NULL COLLATE NOCASE UNIQUE,
    nombre TEXT NOT NULL,
    tipo TEXT NOT NULL CHECK (tipo IN ('ACTIVO','PASIVO','PATRIMONIO','INGRESO','GASTO','COSTO')),
    cuenta_padre_id INTEGER,
    imputable INTEGER NOT NULL DEFAULT 1 CHECK (imputable IN (0,1)),
    activa INTEGER NOT NULL DEFAULT 1 CHECK (activa IN (0,1)),
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (tipo) REFERENCES tipo_cuenta_contable(codigo),
    FOREIGN KEY (cuenta_padre_id) REFERENCES cuenta_contable(id)
);

CREATE TABLE IF NOT EXISTS tipo_diario_contable (
    codigo TEXT PRIMARY KEY,
    nombre TEXT NOT NULL,
    descripcion TEXT,
    activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0,1)),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS asiento_contable (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    numero_asiento TEXT NOT NULL UNIQUE,
    tipo_diario_codigo TEXT NOT NULL DEFAULT 'GENERAL',
    fecha_asiento TEXT NOT NULL DEFAULT (date('now')),
    periodo_anio INTEGER NOT NULL CHECK (periodo_anio >= 2000),
    periodo_mes INTEGER NOT NULL CHECK (periodo_mes BETWEEN 1 AND 12),
    concepto TEXT NOT NULL,
    estado TEXT NOT NULL DEFAULT 'REGISTRADO' CHECK (estado IN ('BORRADOR','REGISTRADO','ANULADO','REVERSADO')),
    origen_tipo TEXT,
    origen_id INTEGER,
    total_debe NUMERIC NOT NULL DEFAULT 0 CHECK (total_debe >= 0),
    total_haber NUMERIC NOT NULL DEFAULT 0 CHECK (total_haber >= 0),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (tipo_diario_codigo) REFERENCES tipo_diario_contable(codigo),
    CHECK (total_debe = total_haber)
);

CREATE TABLE IF NOT EXISTS asiento_contable_detalle (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    asiento_id INTEGER NOT NULL,
    cuenta_id INTEGER NOT NULL,
    linea INTEGER NOT NULL CHECK (linea > 0),
    descripcion TEXT,
    debe NUMERIC NOT NULL DEFAULT 0 CHECK (debe >= 0),
    haber NUMERIC NOT NULL DEFAULT 0 CHECK (haber >= 0),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (asiento_id) REFERENCES asiento_contable(id) ON DELETE CASCADE,
    FOREIGN KEY (cuenta_id) REFERENCES cuenta_contable(id),
    CHECK ((debe > 0 AND haber = 0) OR (haber > 0 AND debe = 0)),
    UNIQUE (asiento_id, linea)
);

CREATE TABLE IF NOT EXISTS plantilla_asiento (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo TEXT NOT NULL COLLATE NOCASE UNIQUE,
    nombre TEXT NOT NULL,
    descripcion TEXT,
    activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0,1)),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS plantilla_asiento_detalle (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    plantilla_id INTEGER NOT NULL,
    cuenta_id INTEGER NOT NULL,
    linea INTEGER NOT NULL CHECK (linea > 0),
    lado TEXT NOT NULL CHECK (lado IN ('DEBE','HABER')),
    descripcion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (plantilla_id) REFERENCES plantilla_asiento(id) ON DELETE CASCADE,
    FOREIGN KEY (cuenta_id) REFERENCES cuenta_contable(id),
    UNIQUE (plantilla_id, linea)
);

CREATE TABLE IF NOT EXISTS regla_contable_evento (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    evento_codigo TEXT NOT NULL COLLATE NOCASE UNIQUE,
    plantilla_id INTEGER,
    descripcion TEXT,
    activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0,1)),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (plantilla_id) REFERENCES plantilla_asiento(id)
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

-- Opcionales mínimos: activos, empleados, indicadores, importaciones y checklist.
CREATE TABLE IF NOT EXISTS tipo_activo_negocio (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo TEXT NOT NULL COLLATE NOCASE UNIQUE,
    nombre TEXT NOT NULL,
    descripcion TEXT,
    estado TEXT NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO','INACTIVO')),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS activo_negocio (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tipo_activo_id INTEGER NOT NULL,
    codigo TEXT COLLATE NOCASE UNIQUE,
    nombre TEXT NOT NULL,
    descripcion TEXT,
    fecha_adquisicion TEXT,
    valor_estimado NUMERIC NOT NULL DEFAULT 0 CHECK (valor_estimado >= 0),
    estado TEXT NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO','EN_REPARACION','DANADO','BAJA','VENDIDO')),
    ubicacion TEXT,
    responsable TEXT,
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (tipo_activo_id) REFERENCES tipo_activo_negocio(id)
);

CREATE TABLE IF NOT EXISTS cargo_empleado (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo TEXT NOT NULL COLLATE NOCASE UNIQUE,
    nombre TEXT NOT NULL,
    descripcion TEXT,
    estado TEXT NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO','INACTIVO')),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS empleado_local (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    cargo_id INTEGER,
    tercero_id INTEGER,
    nombre TEXT NOT NULL,
    identificacion TEXT,
    telefono TEXT,
    fecha_ingreso TEXT,
    estado TEXT NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO','INACTIVO','RETIRADO')),
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (cargo_id) REFERENCES cargo_empleado(id),
    FOREIGN KEY (tercero_id) REFERENCES tercero(id)
);

CREATE TABLE IF NOT EXISTS indicador_operativo (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo TEXT NOT NULL COLLATE NOCASE UNIQUE,
    nombre TEXT NOT NULL,
    descripcion TEXT,
    modulo TEXT NOT NULL,
    orden_visual INTEGER NOT NULL DEFAULT 0,
    activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0,1)),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS consulta_reporte_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario_id INTEGER,
    codigo_reporte TEXT NOT NULL,
    fecha_consulta TEXT NOT NULL DEFAULT (datetime('now')),
    filtros_json TEXT,
    filas_resultado INTEGER NOT NULL DEFAULT 0 CHECK (filas_resultado >= 0),
    exitoso INTEGER NOT NULL DEFAULT 1 CHECK (exitoso IN (0,1)),
    error_mensaje TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (usuario_id) REFERENCES usuario_local(id)
);

CREATE TABLE IF NOT EXISTS plantilla_importacion (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo TEXT NOT NULL COLLATE NOCASE UNIQUE,
    nombre TEXT NOT NULL,
    tipo_importacion TEXT NOT NULL,
    encabezados_csv TEXT NOT NULL,
    descripcion TEXT,
    activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0,1)),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS lote_importacion (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    plantilla_id INTEGER,
    tipo_importacion TEXT NOT NULL,
    archivo_origen TEXT NOT NULL,
    fecha_importacion TEXT NOT NULL DEFAULT (datetime('now')),
    estado TEXT NOT NULL DEFAULT 'RECIBIDO' CHECK (estado IN ('RECIBIDO','VALIDADO','RECHAZADO','PROCESADO')),
    total_filas INTEGER NOT NULL DEFAULT 0 CHECK (total_filas >= 0),
    filas_validas INTEGER NOT NULL DEFAULT 0 CHECK (filas_validas >= 0),
    filas_con_error INTEGER NOT NULL DEFAULT 0 CHECK (filas_con_error >= 0),
    checksum_archivo TEXT,
    observacion TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (plantilla_id) REFERENCES plantilla_importacion(id)
);

CREATE TABLE IF NOT EXISTS error_importacion (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    lote_importacion_id INTEGER NOT NULL,
    numero_fila INTEGER NOT NULL CHECK (numero_fila > 0),
    campo TEXT,
    valor_original TEXT,
    mensaje TEXT NOT NULL,
    severidad TEXT NOT NULL DEFAULT 'ERROR' CHECK (severidad IN ('ADVERTENCIA','ERROR')),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (lote_importacion_id) REFERENCES lote_importacion(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS checklist_operativo (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo TEXT NOT NULL COLLATE NOCASE UNIQUE,
    nombre TEXT NOT NULL,
    descripcion TEXT,
    frecuencia TEXT NOT NULL DEFAULT 'MANUAL' CHECK (frecuencia IN ('DIARIA','SEMANAL','MENSUAL','MANUAL')),
    activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0,1)),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS checklist_item (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    checklist_id INTEGER NOT NULL,
    orden INTEGER NOT NULL DEFAULT 0,
    titulo TEXT NOT NULL,
    descripcion TEXT,
    modulo_relacionado TEXT,
    activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0,1)),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (checklist_id) REFERENCES checklist_operativo(id) ON DELETE CASCADE
);



CREATE INDEX IF NOT EXISTS idx_activo_negocio_estado ON activo_negocio(estado);
CREATE INDEX IF NOT EXISTS idx_activo_negocio_tipo ON activo_negocio(tipo_activo_id);
CREATE INDEX IF NOT EXISTS idx_empleado_local_estado ON empleado_local(estado);
CREATE INDEX IF NOT EXISTS idx_empleado_local_cargo ON empleado_local(cargo_id);
CREATE INDEX IF NOT EXISTS idx_indicador_operativo_modulo ON indicador_operativo(modulo, activo);
CREATE INDEX IF NOT EXISTS idx_consulta_reporte_log_fecha ON consulta_reporte_log(fecha_consulta);
CREATE INDEX IF NOT EXISTS idx_lote_importacion_estado ON lote_importacion(estado, fecha_importacion);
CREATE INDEX IF NOT EXISTS idx_error_importacion_lote ON error_importacion(lote_importacion_id);
CREATE INDEX IF NOT EXISTS idx_checklist_item_checklist ON checklist_item(checklist_id, activo);

CREATE INDEX IF NOT EXISTS idx_cuenta_contable_codigo ON cuenta_contable(codigo);
CREATE INDEX IF NOT EXISTS idx_cuenta_contable_tipo ON cuenta_contable(tipo);
CREATE INDEX IF NOT EXISTS idx_asiento_contable_fecha ON asiento_contable(fecha_asiento, estado);
CREATE INDEX IF NOT EXISTS idx_asiento_contable_origen ON asiento_contable(origen_tipo, origen_id);
CREATE INDEX IF NOT EXISTS idx_asiento_detalle_asiento ON asiento_contable_detalle(asiento_id);
CREATE INDEX IF NOT EXISTS idx_asiento_detalle_cuenta ON asiento_contable_detalle(cuenta_id);
CREATE INDEX IF NOT EXISTS idx_regla_contable_evento ON regla_contable_evento(evento_codigo);

CREATE INDEX IF NOT EXISTS idx_tipo_comprobante_local_estado ON tipo_comprobante_local(estado);
CREATE INDEX IF NOT EXISTS idx_impuesto_configuracion_estado ON impuesto_configuracion(estado);
CREATE INDEX IF NOT EXISTS idx_documento_fiscal_fecha ON documento_fiscal_preparado(fecha_emision, estado);
CREATE INDEX IF NOT EXISTS idx_documento_fiscal_tercero ON documento_fiscal_preparado(tercero_id);
CREATE INDEX IF NOT EXISTS idx_documento_fiscal_venta ON documento_fiscal_preparado(venta_interna_id);
CREATE INDEX IF NOT EXISTS idx_documento_fiscal_compra ON documento_fiscal_preparado(compra_id);
CREATE INDEX IF NOT EXISTS idx_documento_fiscal_detalle_doc ON documento_fiscal_preparado_detalle(documento_id);

CREATE INDEX IF NOT EXISTS idx_usuario_local_nombre ON usuario_local(nombre_usuario);
CREATE INDEX IF NOT EXISTS idx_rol_local_codigo ON rol_local(codigo);
CREATE INDEX IF NOT EXISTS idx_permiso_local_codigo ON permiso_local(codigo);
CREATE INDEX IF NOT EXISTS idx_permiso_local_modulo ON permiso_local(modulo);

CREATE INDEX IF NOT EXISTS idx_auditoria_fecha ON auditoria_evento(fecha_evento);
CREATE INDEX IF NOT EXISTS idx_auditoria_usuario ON auditoria_evento(usuario_id);
CREATE INDEX IF NOT EXISTS idx_auditoria_modulo_accion ON auditoria_evento(modulo, accion);
CREATE INDEX IF NOT EXISTS idx_auditoria_entidad ON auditoria_evento(entidad, entidad_id);

CREATE INDEX IF NOT EXISTS idx_tipo_movimiento_inventario_estado ON tipo_movimiento_inventario(estado);
CREATE INDEX IF NOT EXISTS idx_conteo_inventario_estado ON conteo_inventario(estado, fecha_conteo);
CREATE INDEX IF NOT EXISTS idx_conteo_detalle_producto ON conteo_inventario_detalle(producto_id);
CREATE INDEX IF NOT EXISTS idx_ajuste_inventario_fecha ON ajuste_inventario(fecha_ajuste, estado);
CREATE INDEX IF NOT EXISTS idx_ajuste_detalle_producto ON ajuste_inventario_detalle(producto_id);

CREATE INDEX IF NOT EXISTS idx_producto_nombre ON producto(nombre);
CREATE INDEX IF NOT EXISTS idx_producto_categoria ON producto(categoria_id);
CREATE INDEX IF NOT EXISTS idx_producto_estado ON producto(estado);
CREATE INDEX IF NOT EXISTS idx_producto_stock_bajo ON producto(stock_actual, stock_minimo);
CREATE INDEX IF NOT EXISTS idx_lote_producto_vencimiento ON lote_producto(fecha_vencimiento);
CREATE INDEX IF NOT EXISTS idx_compra_fecha ON compra(fecha_compra);
CREATE INDEX IF NOT EXISTS idx_movimiento_producto_fecha ON movimiento_inventario(producto_id, fecha_movimiento);
CREATE INDEX IF NOT EXISTS idx_venta_fecha ON venta_interna(fecha_venta);
CREATE INDEX IF NOT EXISTS idx_venta_cliente ON venta_interna(cliente_fiado_id);
CREATE INDEX IF NOT EXISTS idx_venta_pago_venta ON venta_pago(venta_interna_id);
CREATE INDEX IF NOT EXISTS idx_merma_fecha ON merma_retiro(fecha_retiro);
CREATE INDEX IF NOT EXISTS idx_cuenta_cliente_estado ON cuenta_por_cobrar(cliente_fiado_id, estado);


CREATE INDEX IF NOT EXISTS idx_tercero_nombre ON tercero(nombre_comercial, nombre_legal);
CREATE INDEX IF NOT EXISTS idx_tercero_identificacion ON tercero(tipo_identificacion, numero_identificacion);
CREATE INDEX IF NOT EXISTS idx_tercero_contacto_tercero ON tercero_contacto(tercero_id);
CREATE INDEX IF NOT EXISTS idx_tercero_direccion_tercero ON tercero_direccion(tercero_id);

COMMIT;

CREATE INDEX IF NOT EXISTS idx_movimiento_caja_fecha ON movimiento_caja(fecha_movimiento);
CREATE INDEX IF NOT EXISTS idx_gasto_operativo_caja ON gasto_operativo(caja_diaria_id);
CREATE INDEX IF NOT EXISTS idx_gasto_operativo_tipo ON gasto_operativo(tipo_gasto_id);
CREATE INDEX IF NOT EXISTS idx_arqueo_caja_caja ON arqueo_caja(caja_diaria_id);
