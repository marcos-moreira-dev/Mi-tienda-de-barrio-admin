$ErrorActionPreference = 'Stop'
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = Resolve-Path (Join-Path $scriptDir '..\..')
$schema = Join-Path $root 'database\sql\migrations\V001__schema_erp_local_sqlite_consolidado.sql'
$seed = Join-Path $root 'database\sql\seeds\V001__seed_inicial_cliente.sql'
$smoke = Join-Path $root 'database\sql\checks\V001__smoke_check.sql'
$logDir = Join-Path $root '.diagnostics\logs'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$log = Join-Path $logDir "validate-caja-gastos_$timestamp.log"

Write-Host '== MiTienda :: validacion caja y gastos =='
Write-Host "Log: $log"

$required = @(
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\caja\TipoGasto.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\caja\GastoOperativo.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\caja\RegistroGastoOperativo.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\caja\ArqueoCaja.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\caja\RegistroArqueoCaja.java'
)
foreach ($rel in $required) {
    $path = Join-Path $root $rel
    if (-not (Test-Path $path)) { throw "Falta archivo requerido: $rel" }
}

$cajaService = Get-Content (Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\application\caja\CajaDiariaService.java') -Raw
$cajaRepo = Get-Content (Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\infrastructure\caja\SqliteCajaDiariaRepository.java') -Raw
$appBootstrap = Get-Content (Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\bootstrap\AppBootstrap.java') -Raw
if ($cajaService -notmatch 'registrarGastoOperativo') { throw 'CajaDiariaService no contiene registrarGastoOperativo.' }
if ($cajaService -notmatch 'registrarArqueo') { throw 'CajaDiariaService no contiene registrarArqueo.' }
if ($cajaRepo -notmatch 'gasto_operativo') { throw 'SqliteCajaDiariaRepository no opera gasto_operativo.' }
if ($cajaRepo -notmatch 'arqueo_caja') { throw 'SqliteCajaDiariaRepository no opera arqueo_caja.' }
if ($appBootstrap -notmatch 'CajaDiariaService\(new SqliteCajaDiariaRepository\(connectionFactory\), writeAccessGuard, auditoriaService\)') { throw 'AppBootstrap no conecta caja con auditoriaService.' }

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
for table in ['tipo_movimiento_caja','forma_pago_local','tipo_gasto','gasto_operativo','arqueo_caja']:
    count = conn.execute("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?", (table,)).fetchone()[0]
    if count != 1:
        raise SystemExit(f'Falta tabla {table}')
if conn.execute('SELECT COUNT(*) FROM tipo_movimiento_caja').fetchone()[0] < 6:
    raise SystemExit('Faltan tipos de movimiento de caja')
if conn.execute('SELECT COUNT(*) FROM forma_pago_local').fetchone()[0] < 3:
    raise SystemExit('Faltan formas de pago')
if conn.execute('SELECT COUNT(*) FROM tipo_gasto').fetchone()[0] < 4:
    raise SystemExit('Faltan tipos de gasto')
conn.execute("INSERT INTO caja_diaria(fecha,saldo_inicial,saldo_esperado) VALUES('2026-05-26', 100, 100)")
caja_id = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
tipo_gasto_id = conn.execute("SELECT id FROM tipo_gasto WHERE nombre='Internet'").fetchone()[0]
conn.execute("INSERT INTO movimiento_caja(caja_diaria_id,tipo_movimiento,origen,monto,metodo_pago,descripcion) VALUES(?,?,?,?,?,?)", (caja_id,'EGRESO','GASTO_OPERATIVO',25,'EFECTIVO','Pago internet'))
mov_id = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO gasto_operativo(caja_diaria_id,tipo_gasto_id,movimiento_caja_id,monto,forma_pago_codigo,descripcion) VALUES(?,?,?,?,?,?)", (caja_id,tipo_gasto_id,mov_id,25,'EFECTIVO','Pago internet'))
gasto_id = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute('UPDATE movimiento_caja SET referencia_id=? WHERE id=?', (gasto_id, mov_id))
conn.execute("UPDATE caja_diaria SET total_egresos=25, saldo_esperado=75 WHERE id=?", (caja_id,))
conn.execute("INSERT INTO arqueo_caja(caja_diaria_id,saldo_sistema,saldo_contado,diferencia,responsable_texto) VALUES(?,?,?,?,?)", (caja_id,75,74,-1,'Validacion'))
if conn.execute('SELECT saldo_esperado FROM caja_diaria WHERE id=?',(caja_id,)).fetchone()[0] != 75:
    raise SystemExit('Saldo esperado incorrecto')
errors = conn.execute('PRAGMA foreign_key_check').fetchall()
if errors:
    raise SystemExit(f'Errores FK: {errors}')
print('OK caja/gastos: tablas, seed, gasto, arqueo y FK correctos.')
'@
$tmp = Join-Path $env:TEMP "mitienda_caja_gastos_$timestamp.py"
$code | Set-Content -Path $tmp -Encoding UTF8
& $python.Source $tmp $schema $seed $smoke 2>&1 | Tee-Object -FilePath $log
if ($LASTEXITCODE -ne 0) { throw "La validacion de caja/gastos fallo con codigo $LASTEXITCODE." }
Write-Host '[OK] Caja y gastos validados.'
