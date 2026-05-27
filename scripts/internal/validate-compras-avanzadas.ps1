$ErrorActionPreference = 'Stop'
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = Resolve-Path (Join-Path $scriptDir '..\..')
$schema = Join-Path $root 'database\sql\migrations\V001__schema_erp_local_sqlite_consolidado.sql'
$seed = Join-Path $root 'database\sql\seeds\V001__seed_inicial_cliente.sql'
$smoke = Join-Path $root 'database\sql\checks\V001__smoke_check.sql'
$logDir = Join-Path $root '.diagnostics\logs'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$log = Join-Path $logDir "validate-compras-avanzadas_$timestamp.log"

Write-Host '== MiTienda :: validacion compras avanzadas =='
Write-Host "Log: $log"

$required = @(
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\compra\RegistroCompraAvanzada.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\compra\DetalleCompraAvanzada.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\compra\CuentaPorPagar.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\compra\RegistroPagoProveedor.java'
)
foreach ($rel in $required) {
    $path = Join-Path $root $rel
    if (-not (Test-Path $path)) { throw "Falta archivo requerido: $rel" }
}

$service = Get-Content (Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\application\compra\CompraService.java') -Raw
$repo = Get-Content (Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\application\compra\CompraRepository.java') -Raw
$sqliteRepo = Get-Content (Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\infrastructure\compra\SqliteCompraRepository.java') -Raw
$appBootstrap = Get-Content (Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\bootstrap\AppBootstrap.java') -Raw
if ($service -notmatch 'registrarAvanzada') { throw 'CompraService no expone registrarAvanzada.' }
if ($service -notmatch 'registrarPagoProveedor') { throw 'CompraService no expone registrarPagoProveedor.' }
if ($repo -notmatch 'registrarCompraAvanzada') { throw 'CompraRepository no declara registrarCompraAvanzada.' }
if ($repo -notmatch 'listarCuentasPorPagarPendientes') { throw 'CompraRepository no lista cuentas por pagar.' }
if ($sqliteRepo -notmatch 'cuenta_por_pagar') { throw 'SqliteCompraRepository no usa cuenta_por_pagar.' }
if ($appBootstrap -notmatch 'CompraService\(new SqliteCompraRepository\(connectionFactory\), writeAccessGuard, auditoriaService\)') { throw 'AppBootstrap no conecta CompraService con AuditoriaService.' }

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
for table in ['cuenta_por_pagar', 'pago_proveedor']:
    exists = conn.execute("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?", (table,)).fetchone()[0]
    if exists != 1:
        raise SystemExit(f'Falta tabla {table}')
cat = conn.execute("SELECT id FROM categoria LIMIT 1").fetchone()[0]
udm = conn.execute("SELECT id FROM unidad_medida LIMIT 1").fetchone()[0]
prov = conn.execute("SELECT id FROM proveedor LIMIT 1").fetchone()[0]
conn.execute("INSERT INTO producto(nombre,categoria_id,unidad_medida_id,stock_actual,stock_minimo,precio_venta) VALUES('Compra avanzada producto A',?,?,0,1,1)", (cat, udm))
p1 = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO producto(nombre,categoria_id,unidad_medida_id,stock_actual,stock_minimo,precio_venta) VALUES('Compra avanzada producto B',?,?,5,1,1)", (cat, udm))
p2 = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO compra(proveedor_id,fecha_compra,tipo_comprobante,total_estimado,observacion) VALUES(?,?,?,?,?)", (prov, '2026-05-26', 'NOTA', 18.5, 'Compra a credito validacion'))
compra = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO detalle_compra(compra_id,producto_id,cantidad,costo_unitario,subtotal) VALUES(?,?,?,?,?)", (compra, p1, 3, 2.5, 7.5))
conn.execute("INSERT INTO detalle_compra(compra_id,producto_id,cantidad,costo_unitario,subtotal) VALUES(?,?,?,?,?)", (compra, p2, 2, 5.5, 11.0))
conn.execute("UPDATE producto SET stock_actual = stock_actual + 3 WHERE id=?", (p1,))
conn.execute("UPDATE producto SET stock_actual = stock_actual + 2 WHERE id=?", (p2,))
conn.execute("INSERT INTO movimiento_inventario(producto_id,tipo_movimiento,cantidad,stock_anterior,stock_nuevo,referencia_tipo,referencia_id,motivo) VALUES(?,?,?,?,?,?,?,?)", (p1, 'ENTRADA_COMPRA', 3, 0, 3, 'COMPRA', compra, 'Validacion compra avanzada'))
conn.execute("INSERT INTO movimiento_inventario(producto_id,tipo_movimiento,cantidad,stock_anterior,stock_nuevo,referencia_tipo,referencia_id,motivo) VALUES(?,?,?,?,?,?,?,?)", (p2, 'ENTRADA_COMPRA', 2, 5, 7, 'COMPRA', compra, 'Validacion compra avanzada'))
conn.execute("INSERT INTO cuenta_por_pagar(compra_id,proveedor_id,fecha_emision,fecha_vencimiento,monto_total,saldo_pendiente) VALUES(?,?,?,?,?,?)", (compra, prov, '2026-05-26', '2026-06-02', 18.5, 18.5))
cuenta = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO pago_proveedor(cuenta_por_pagar_id,fecha_pago,monto,forma_pago,referencia) VALUES(?,?,?,?,?)", (cuenta, '2026-05-27', 8.5, 'EFECTIVO', 'ABONO-1'))
conn.execute("UPDATE cuenta_por_pagar SET saldo_pendiente=10.0, estado='PARCIAL' WHERE id=?", (cuenta,))
conn.execute("INSERT INTO pago_proveedor(cuenta_por_pagar_id,fecha_pago,monto,forma_pago,referencia) VALUES(?,?,?,?,?)", (cuenta, '2026-05-28', 10.0, 'EFECTIVO', 'SALDO'))
conn.execute("UPDATE cuenta_por_pagar SET saldo_pendiente=0, estado='PAGADA' WHERE id=?", (cuenta,))
if conn.execute('SELECT COUNT(*) FROM detalle_compra WHERE compra_id=?', (compra,)).fetchone()[0] != 2:
    raise SystemExit('La compra avanzada no tiene dos detalles')
if conn.execute('SELECT saldo_pendiente FROM cuenta_por_pagar WHERE id=?', (cuenta,)).fetchone()[0] != 0:
    raise SystemExit('La cuenta por pagar no quedo saldada')
errors = conn.execute('PRAGMA foreign_key_check').fetchall()
if errors:
    raise SystemExit(f'Errores FK: {errors}')
print('OK compras avanzadas: compra multiple, cuenta por pagar, pagos y FK correctos.')
'@
$tmp = Join-Path $env:TEMP "mitienda_compras_avanzadas_$timestamp.py"
$code | Set-Content -Path $tmp -Encoding UTF8
& $python.Source $tmp $schema $seed $smoke 2>&1 | Tee-Object -FilePath $log
if ($LASTEXITCODE -ne 0) { throw "La validacion de compras avanzadas fallo con codigo $LASTEXITCODE." }
Write-Host '[OK] Compras avanzadas validadas.'
