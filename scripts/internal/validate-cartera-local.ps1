$ErrorActionPreference = 'Stop'
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = Resolve-Path (Join-Path $scriptDir '..\..')
$schema = Join-Path $root 'database\sql\migrations\V001__schema_erp_local_sqlite_consolidado.sql'
$seed = Join-Path $root 'database\sql\seeds\V001__seed_inicial_cliente.sql'
$smoke = Join-Path $root 'database\sql\checks\V001__smoke_check.sql'
$logDir = Join-Path $root '.diagnostics\logs'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$log = Join-Path $logDir "validate-cartera-local_$timestamp.log"

Write-Host '== MiTienda :: validacion cartera local conectada a caja =='
Write-Host "Log: $log"

$required = @(
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\application\cartera\CarteraLocalService.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\application\cartera\CarteraLocalRepository.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\infrastructure\cartera\SqliteCarteraLocalRepository.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\cartera\RegistroAbonoConCaja.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\cartera\RegistroPagoProveedorConCaja.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\cartera\RegistroVentaPagadaEnCaja.java'
)
foreach ($rel in $required) {
    $path = Join-Path $root $rel
    if (-not (Test-Path $path)) { throw "Falta archivo requerido: $rel" }
}

$appContext = Get-Content (Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\bootstrap\AppContext.java') -Raw
$appBootstrap = Get-Content (Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\bootstrap\AppBootstrap.java') -Raw
$repo = Get-Content (Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\infrastructure\cartera\SqliteCarteraLocalRepository.java') -Raw
if ($appContext -notmatch 'CarteraLocalService') { throw 'AppContext no expone CarteraLocalService.' }
if ($appBootstrap -notmatch 'SqliteCarteraLocalRepository') { throw 'AppBootstrap no conecta SqliteCarteraLocalRepository.' }
if ($repo -notmatch 'registrarAbonoConCaja') { throw 'Repositorio de cartera no registra abono con caja.' }
if ($repo -notmatch 'registrarPagoProveedorConCaja') { throw 'Repositorio de cartera no registra pago proveedor con caja.' }
if ($repo -notmatch 'registrarVentaPagadaEnCaja') { throw 'Repositorio de cartera no conecta venta pagada con caja.' }

$python = Get-Command python -ErrorAction SilentlyContinue
if (-not $python) { $python = Get-Command py -ErrorAction SilentlyContinue }
if (-not $python) { throw 'No se encontro Python para validar SQLite temporalmente.' }

$code = @'
import pathlib, sqlite3, sys
from decimal import Decimal
schema = pathlib.Path(sys.argv[1])
seed = pathlib.Path(sys.argv[2])
smoke = pathlib.Path(sys.argv[3])
conn = sqlite3.connect(':memory:')
conn.execute('PRAGMA foreign_keys=ON')
conn.executescript(schema.read_text(encoding='utf-8'))
conn.executescript(seed.read_text(encoding='utf-8'))
conn.executescript(smoke.read_text(encoding='utf-8'))
for table, column in [('abono','movimiento_caja_id'),('pago_proveedor','movimiento_caja_id'),('venta_pago','movimiento_caja_id')]:
    cols = [row[1] for row in conn.execute(f"PRAGMA table_info({table})")]
    if column not in cols:
        raise SystemExit(f'Falta columna {table}.{column}')
# Datos base mínimos
conn.execute("INSERT INTO categoria(nombre) VALUES('General Validacion')")
cat = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO unidad_medida(nombre,abreviatura) VALUES('Unidad Validacion','uv')")
uni = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO proveedor(nombre,telefono) VALUES('Proveedor Validacion','0990000000')")
prov = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO cliente_fiado(nombre,telefono) VALUES('Cliente Validacion','0980000000')")
cliente = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO producto(codigo_interno,nombre,categoria_id,unidad_medida_id,proveedor_principal_id,precio_compra_referencia,precio_venta,stock_actual,stock_minimo) VALUES('P001','Producto validacion',?,?,?,10,15,20,1)", (cat, uni, prov))
producto = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO caja_diaria(fecha,saldo_inicial,saldo_esperado) VALUES('2026-05-26', 100, 100)")
caja = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
# Venta pagada pendiente de conectar a caja
conn.execute("INSERT INTO venta_interna(total,metodo_pago,advertencia_tributaria_aceptada) VALUES(30,'EFECTIVO',1)")
venta = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO detalle_venta_interna(venta_interna_id,producto_id,cantidad,precio_unitario,subtotal) VALUES(?,?,?,?,?)", (venta, producto, 2, 15, 30))
conn.execute("INSERT INTO venta_pago(venta_interna_id,monto,metodo_pago) VALUES(?,?,?)", (venta, 30, 'EFECTIVO'))
venta_pago = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO movimiento_caja(caja_diaria_id,tipo_movimiento,origen,referencia_id,monto,metodo_pago,descripcion) VALUES(?,?,?,?,?,?,?)", (caja,'INGRESO','VENTA_INTERNA',venta,30,'EFECTIVO','Ingreso venta'))
mov_venta = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute('UPDATE venta_pago SET movimiento_caja_id=? WHERE id=?', (mov_venta, venta_pago))
# Venta fiada -> cuenta por cobrar -> abono conectado a caja
conn.execute("INSERT INTO venta_interna(cliente_fiado_id,total,metodo_pago,advertencia_tributaria_aceptada) VALUES(?,?,?,1)", (cliente, 40, 'FIADO'))
venta_fiada = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO cuenta_por_cobrar(cliente_fiado_id,venta_interna_id,monto_original,saldo_pendiente,observacion) VALUES(?,?,?,?,?)", (cliente, venta_fiada, 40, 40, 'Cuenta validacion'))
cuenta_cobrar = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO movimiento_caja(caja_diaria_id,tipo_movimiento,origen,monto,metodo_pago,descripcion) VALUES(?,?,?,?,?,?)", (caja,'INGRESO','ABONO_FIADO',15,'EFECTIVO','Abono fiado'))
mov_abono = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO abono(cuenta_por_cobrar_id,movimiento_caja_id,monto,metodo_pago,observacion) VALUES(?,?,?,?,?)", (cuenta_cobrar,mov_abono,15,'EFECTIVO','Abono validacion'))
abono = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute('UPDATE movimiento_caja SET referencia_id=? WHERE id=?', (abono, mov_abono))
conn.execute("UPDATE cuenta_por_cobrar SET saldo_pendiente=25, estado='ABIERTA' WHERE id=?", (cuenta_cobrar,))
# Compra a crédito -> cuenta por pagar -> pago conectado a caja
conn.execute("INSERT INTO compra(proveedor_id,total_estimado,estado) VALUES(?,?,?)", (prov, 60, 'REGISTRADA'))
compra = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO cuenta_por_pagar(compra_id,proveedor_id,monto_total,saldo_pendiente,estado) VALUES(?,?,?,?,?)", (compra, prov, 60, 60, 'PENDIENTE'))
cuenta_pagar = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO movimiento_caja(caja_diaria_id,tipo_movimiento,origen,monto,metodo_pago,descripcion) VALUES(?,?,?,?,?,?)", (caja,'EGRESO','PAGO_PROVEEDOR',20,'EFECTIVO','Pago proveedor'))
mov_pago = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO pago_proveedor(cuenta_por_pagar_id,movimiento_caja_id,monto,forma_pago,referencia,observacion) VALUES(?,?,?,?,?,?)", (cuenta_pagar,mov_pago,20,'EFECTIVO','VALID','Pago validacion'))
pago = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute('UPDATE movimiento_caja SET referencia_id=? WHERE id=?', (pago, mov_pago))
conn.execute("UPDATE cuenta_por_pagar SET saldo_pendiente=40, estado='PARCIAL' WHERE id=?", (cuenta_pagar,))
# Recalcular caja
conn.execute("UPDATE caja_diaria SET total_ingresos=COALESCE((SELECT SUM(monto) FROM movimiento_caja WHERE caja_diaria_id=? AND tipo_movimiento='INGRESO'),0), total_egresos=COALESCE((SELECT SUM(monto) FROM movimiento_caja WHERE caja_diaria_id=? AND tipo_movimiento='EGRESO'),0), saldo_esperado=saldo_inicial+COALESCE((SELECT SUM(monto) FROM movimiento_caja WHERE caja_diaria_id=? AND tipo_movimiento='INGRESO'),0)-COALESCE((SELECT SUM(monto) FROM movimiento_caja WHERE caja_diaria_id=? AND tipo_movimiento='EGRESO'),0) WHERE id=?", (caja,caja,caja,caja,caja))
row = conn.execute('SELECT total_ingresos,total_egresos,saldo_esperado FROM caja_diaria WHERE id=?', (caja,)).fetchone()
if tuple(row) != (45, 20, 125):
    raise SystemExit(f'Caja incorrecta: {row}')
if conn.execute('SELECT movimiento_caja_id FROM venta_pago WHERE id=?',(venta_pago,)).fetchone()[0] != mov_venta:
    raise SystemExit('venta_pago no quedo vinculado a caja')
if conn.execute('SELECT movimiento_caja_id FROM abono WHERE id=?',(abono,)).fetchone()[0] != mov_abono:
    raise SystemExit('abono no quedo vinculado a caja')
if conn.execute('SELECT movimiento_caja_id FROM pago_proveedor WHERE id=?',(pago,)).fetchone()[0] != mov_pago:
    raise SystemExit('pago_proveedor no quedo vinculado a caja')
errors = conn.execute('PRAGMA foreign_key_check').fetchall()
if errors:
    raise SystemExit(f'Errores FK: {errors}')
print('OK cartera/caja: venta pagada, abono y pago proveedor vinculados a caja.')
'@
$tmp = Join-Path $env:TEMP "mitienda_cartera_local_$timestamp.py"
$code | Set-Content -Path $tmp -Encoding UTF8
& $python.Source $tmp $schema $seed $smoke 2>&1 | Tee-Object -FilePath $log
if ($LASTEXITCODE -ne 0) { throw "La validacion de cartera local fallo con codigo $LASTEXITCODE." }
Write-Host '[OK] Cartera local conectada a caja validada.'
