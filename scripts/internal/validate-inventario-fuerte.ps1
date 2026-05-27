$ErrorActionPreference = 'Stop'
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = Resolve-Path (Join-Path $scriptDir '..\..')
$schema = Join-Path $root 'database\sql\migrations\V001__schema_erp_local_sqlite_consolidado.sql'
$seed = Join-Path $root 'database\sql\seeds\V001__seed_inicial_cliente.sql'
$smoke = Join-Path $root 'database\sql\checks\V001__smoke_check.sql'
$src = Join-Path $root 'desktop\src\main\java'
$logDir = Join-Path $root '.diagnostics\logs'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$log = Join-Path $logDir "validate-inventario-fuerte_$timestamp.log"

Write-Host '== MiTienda :: validacion inventario fuerte =='
Write-Host "Log: $log"

$required = @(
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\application\inventario\InventarioFuerteService.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\application\inventario\InventarioFuerteRepository.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\infrastructure\inventario\SqliteInventarioFuerteRepository.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\inventario\ConteoInventario.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\inventario\AjusteInventarioSolicitud.java'
)
foreach ($rel in $required) {
    $path = Join-Path $root $rel
    if (-not (Test-Path $path)) { throw "Falta archivo requerido: $rel" }
}

$appContext = Get-Content (Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\bootstrap\AppContext.java') -Raw
$appBootstrap = Get-Content (Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\bootstrap\AppBootstrap.java') -Raw
if ($appContext -notmatch 'inventarioFuerteService') { throw 'AppContext no expone inventarioFuerteService.' }
if ($appBootstrap -notmatch 'SqliteInventarioFuerteRepository') { throw 'AppBootstrap no conecta SqliteInventarioFuerteRepository.' }

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
required_tables = [
    'tipo_movimiento_inventario', 'conteo_inventario', 'conteo_inventario_detalle',
    'ajuste_inventario', 'ajuste_inventario_detalle'
]
for table in required_tables:
    count = conn.execute("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?", (table,)).fetchone()[0]
    if count != 1:
        raise SystemExit(f'Falta tabla {table}')
if conn.execute("SELECT COUNT(*) FROM tipo_movimiento_inventario").fetchone()[0] < 7:
    raise SystemExit('Faltan tipos de movimiento sembrados')
cat = conn.execute("SELECT id FROM categoria LIMIT 1").fetchone()[0]
udm = conn.execute("SELECT id FROM unidad_medida LIMIT 1").fetchone()[0]
conn.execute("INSERT INTO producto(nombre,categoria_id,unidad_medida_id,stock_actual,stock_minimo,precio_venta) VALUES('Producto prueba inventario fuerte',?,?,10,1,1)", (cat, udm))
producto_id = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO conteo_inventario(responsable_texto) VALUES('Validacion')")
conteo_id = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO conteo_inventario_detalle(conteo_id,producto_id,stock_sistema,stock_contado,diferencia) VALUES(?,?,?,?,?)", (conteo_id, producto_id, 10, 8, -2))
conn.execute("INSERT INTO ajuste_inventario(conteo_inventario_id,responsable_texto,motivo) VALUES(?,?,?)", (conteo_id, 'Validacion', 'Ajuste de validacion'))
ajuste_id = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("UPDATE producto SET stock_actual = 8 WHERE id = ?", (producto_id,))
conn.execute("INSERT INTO movimiento_inventario(producto_id,tipo_movimiento,cantidad,stock_anterior,stock_nuevo,referencia_tipo,motivo) VALUES(?,?,?,?,?,?,?)", (producto_id, 'AJUSTE_NEGATIVO', 2, 10, 8, 'AJUSTE_INVENTARIO', 'Ajuste de validacion'))
movimiento_id = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO ajuste_inventario_detalle(ajuste_inventario_id,producto_id,tipo_movimiento,cantidad,stock_anterior,stock_nuevo,movimiento_inventario_id) VALUES(?,?,?,?,?,?,?)", (ajuste_id, producto_id, 'AJUSTE_NEGATIVO', 2, 10, 8, movimiento_id))
errors = conn.execute('PRAGMA foreign_key_check').fetchall()
if errors:
    raise SystemExit(f'Errores FK: {errors}')
print('OK inventario fuerte: tablas, seed, conteo, ajuste y FK correctos.')
'@
$tmp = Join-Path $env:TEMP "mitienda_inventario_fuerte_$timestamp.py"
$code | Set-Content -Path $tmp -Encoding UTF8
& $python.Source $tmp $schema $seed $smoke 2>&1 | Tee-Object -FilePath $log
if ($LASTEXITCODE -ne 0) { throw "La validacion de inventario fuerte fallo con codigo $LASTEXITCODE." }
Write-Host '[OK] Inventario fuerte validado.'
