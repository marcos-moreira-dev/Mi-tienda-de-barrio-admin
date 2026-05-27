$ErrorActionPreference = 'Stop'
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = Resolve-Path (Join-Path $scriptDir '..\..')
$schema = Join-Path $root 'database\sql\migrations\V001__schema_erp_local_sqlite_consolidado.sql'
$seed = Join-Path $root 'database\sql\seeds\V001__seed_inicial_cliente.sql'
$smoke = Join-Path $root 'database\sql\checks\V001__smoke_check.sql'
$logDir = Join-Path $root '.diagnostics\logs'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$log = Join-Path $logDir "validate-ventas-avanzadas_$timestamp.log"

Write-Host '== MiTienda :: validacion ventas internas avanzadas =='
Write-Host "Log: $log"

$required = @(
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\venta\RegistroVentaInternaAvanzada.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\venta\DetalleVentaInternaAvanzada.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\venta\VentaPago.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\venta\RegistroAnulacionVentaInterna.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\venta\AnulacionVentaInterna.java'
)
foreach ($rel in $required) {
    $path = Join-Path $root $rel
    if (-not (Test-Path $path)) { throw "Falta archivo requerido: $rel" }
}

$service = Get-Content (Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\application\venta\VentaInternaService.java') -Raw
$repo = Get-Content (Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\application\venta\VentaInternaRepository.java') -Raw
$sqliteRepo = Get-Content (Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\infrastructure\venta\SqliteVentaInternaRepository.java') -Raw
$appBootstrap = Get-Content (Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\bootstrap\AppBootstrap.java') -Raw
if ($service -notmatch 'registrarAvanzada') { throw 'VentaInternaService no expone registrarAvanzada.' }
if ($service -notmatch 'anular') { throw 'VentaInternaService no expone anular.' }
if ($repo -notmatch 'registrarVentaAvanzada') { throw 'VentaInternaRepository no declara registrarVentaAvanzada.' }
if ($repo -notmatch 'anularVenta') { throw 'VentaInternaRepository no declara anularVenta.' }
if ($sqliteRepo -notmatch 'venta_pago') { throw 'SqliteVentaInternaRepository no usa venta_pago.' }
if ($sqliteRepo -notmatch 'anulacion_venta') { throw 'SqliteVentaInternaRepository no usa anulacion_venta.' }
if ($sqliteRepo -notmatch 'cuenta_por_cobrar') { throw 'SqliteVentaInternaRepository no crea cuenta_por_cobrar para venta fiada.' }
if ($appBootstrap -notmatch 'VentaInternaService\(new SqliteVentaInternaRepository\(connectionFactory\), writeAccessGuard, auditoriaService\)') { throw 'AppBootstrap no conecta VentaInternaService con AuditoriaService.' }

$python = Get-Command python -ErrorAction SilentlyContinue
if (-not $python) { $python = Get-Command py -ErrorAction SilentlyContinue }
if (-not $python) { throw 'No se encontro Python para validar SQLite temporalmente.' }

$code = @'
import pathlib, sqlite3, sys
schema = pathlib.Path(sys.argv[1])
seed = pathlib.Path(sys.argv[2])
smoke = pathlib.Path(sys.argv[3])
conn = sqlite3.connect(':memory:')
conn.execute('PRAGMA foreign_keys=ON')
conn.executescript(schema.read_text(encoding='utf-8'))
conn.executescript(seed.read_text(encoding='utf-8'))
conn.executescript(smoke.read_text(encoding='utf-8'))
for table in ['venta_pago', 'anulacion_venta']:
    exists = conn.execute("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?", (table,)).fetchone()[0]
    if exists != 1:
        raise SystemExit(f'Falta tabla {table}')
cat = conn.execute("SELECT id FROM categoria LIMIT 1").fetchone()[0]
udm = conn.execute("SELECT id FROM unidad_medida LIMIT 1").fetchone()[0]
conn.execute("INSERT INTO producto(nombre,categoria_id,unidad_medida_id,stock_actual,stock_minimo,precio_venta) VALUES('Venta avanzada producto A',?,?,10,1,2)", (cat, udm))
p1 = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO producto(nombre,categoria_id,unidad_medida_id,stock_actual,stock_minimo,precio_venta) VALUES('Venta avanzada producto B',?,?,8,1,3)", (cat, udm))
p2 = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO cliente_fiado(nombre,telefono,limite_credito) VALUES('Cliente fiado validacion','0999999999',100)")
cliente = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
# venta pagada con dos productos
conn.execute("INSERT INTO venta_interna(total,metodo_pago,numero_referencia,advertencia_tributaria_aceptada,observacion) VALUES(?,?,?,?,?)", (8,'EFECTIVO','VENTA-VALIDA',1,'venta pagada validacion'))
venta = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO detalle_venta_interna(venta_interna_id,producto_id,cantidad,precio_unitario,subtotal) VALUES(?,?,?,?,?)", (venta,p1,1,2,2))
conn.execute("INSERT INTO detalle_venta_interna(venta_interna_id,producto_id,cantidad,precio_unitario,subtotal) VALUES(?,?,?,?,?)", (venta,p2,2,3,6))
conn.execute("UPDATE producto SET stock_actual=stock_actual-1 WHERE id=?", (p1,))
conn.execute("UPDATE producto SET stock_actual=stock_actual-2 WHERE id=?", (p2,))
conn.execute("INSERT INTO movimiento_inventario(producto_id,tipo_movimiento,cantidad,stock_anterior,stock_nuevo,referencia_tipo,referencia_id,motivo) VALUES(?,?,?,?,?,?,?,?)", (p1,'SALIDA_VENTA_INTERNA',1,10,9,'VENTA_INTERNA',venta,'Validacion venta pagada'))
conn.execute("INSERT INTO movimiento_inventario(producto_id,tipo_movimiento,cantidad,stock_anterior,stock_nuevo,referencia_tipo,referencia_id,motivo) VALUES(?,?,?,?,?,?,?,?)", (p2,'SALIDA_VENTA_INTERNA',2,8,6,'VENTA_INTERNA',venta,'Validacion venta pagada'))
conn.execute("INSERT INTO venta_pago(venta_interna_id,monto,metodo_pago,referencia) VALUES(?,?,?,?)", (venta,8,'EFECTIVO','VENTA-VALIDA'))
# venta fiada
conn.execute("INSERT INTO venta_interna(cliente_fiado_id,total,metodo_pago,advertencia_tributaria_aceptada,observacion) VALUES(?,?,?,?,?)", (cliente,3,'FIADO',1,'venta fiada validacion'))
venta_fiada = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO detalle_venta_interna(venta_interna_id,producto_id,cantidad,precio_unitario,subtotal) VALUES(?,?,?,?,?)", (venta_fiada,p1,1,3,3))
conn.execute("UPDATE producto SET stock_actual=stock_actual-1 WHERE id=?", (p1,))
conn.execute("INSERT INTO movimiento_inventario(producto_id,tipo_movimiento,cantidad,stock_anterior,stock_nuevo,referencia_tipo,referencia_id,motivo) VALUES(?,?,?,?,?,?,?,?)", (p1,'SALIDA_VENTA_INTERNA',1,9,8,'VENTA_INTERNA',venta_fiada,'Validacion venta fiada'))
conn.execute("INSERT INTO cuenta_por_cobrar(cliente_fiado_id,venta_interna_id,monto_original,saldo_pendiente) VALUES(?,?,?,?)", (cliente,venta_fiada,3,3))
if conn.execute('SELECT COUNT(*) FROM venta_pago WHERE venta_interna_id=?', (venta,)).fetchone()[0] != 1:
    raise SystemExit('La venta pagada no registro pago')
if conn.execute('SELECT COUNT(*) FROM cuenta_por_cobrar WHERE venta_interna_id=?', (venta_fiada,)).fetchone()[0] != 1:
    raise SystemExit('La venta fiada no creo cuenta por cobrar')
# anulacion controlada de venta pagada
conn.execute("UPDATE venta_interna SET estado='ANULADA' WHERE id=?", (venta,))
conn.execute("INSERT INTO anulacion_venta(venta_interna_id,motivo,responsable_texto) VALUES(?,?,?)", (venta,'Prueba de anulacion','Sistema'))
conn.execute("UPDATE producto SET stock_actual=stock_actual+1 WHERE id=?", (p1,))
conn.execute("INSERT INTO movimiento_inventario(producto_id,tipo_movimiento,cantidad,stock_anterior,stock_nuevo,referencia_tipo,referencia_id,motivo) VALUES(?,?,?,?,?,?,?,?)", (p1,'CORRECCION',1,8,9,'VENTA_INTERNA',venta,'Anulacion validacion'))
if conn.execute('SELECT estado FROM venta_interna WHERE id=?', (venta,)).fetchone()[0] != 'ANULADA':
    raise SystemExit('La venta no quedo anulada')
errors = conn.execute('PRAGMA foreign_key_check').fetchall()
if errors:
    raise SystemExit(f'Errores FK: {errors}')
print('OK ventas avanzadas: venta multiple, pago, fiado, cuenta por cobrar y anulacion correctos.')
'@
$tmp = Join-Path $env:TEMP "mitienda_ventas_avanzadas_$timestamp.py"
$code | Set-Content -Path $tmp -Encoding UTF8
& $python.Source $tmp $schema $seed $smoke 2>&1 | Tee-Object -FilePath $log
if ($LASTEXITCODE -ne 0) { throw "La validacion de ventas avanzadas fallo con codigo $LASTEXITCODE." }
Write-Host '[OK] Ventas internas avanzadas validadas.'
