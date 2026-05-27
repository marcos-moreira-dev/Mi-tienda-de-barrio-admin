$ErrorActionPreference = 'Stop'
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = Resolve-Path (Join-Path $scriptDir '..\..')
$schema = Join-Path $root 'database\sql\migrations\V001__schema_erp_local_sqlite_consolidado.sql'
$seed = Join-Path $root 'database\sql\seeds\V001__seed_inicial_cliente.sql'
$smoke = Join-Path $root 'database\sql\checks\V001__smoke_check.sql'
$logDir = Join-Path $root '.diagnostics\logs'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$log = Join-Path $logDir "validate-sql-local_$timestamp.log"

Write-Host '== MiTienda :: validacion SQL local =='
Write-Host "Schema: $schema"
Write-Host "Seed: $seed"
Write-Host "Smoke: $smoke"
Write-Host "Log: $log"

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
for path in (schema, seed):
    conn.executescript(path.read_text(encoding='utf-8'))
# Ejecutar conteos principales del smoke por compatibilidad.
conn.executescript(smoke.read_text(encoding='utf-8'))
errors = conn.execute('PRAGMA foreign_key_check').fetchall()
if errors:
    print('FOREIGN_KEY_ERRORS', errors)
    raise SystemExit(1)
print('OK SQL local: schema + seed + smoke sin errores de claves foraneas.')
print('Tablas:', len(conn.execute("SELECT name FROM sqlite_master WHERE type='table'").fetchall()))
'@
$tmp = Join-Path $env:TEMP "mitienda_sql_validate_$timestamp.py"
$code | Set-Content -Path $tmp -Encoding UTF8
& $python.Source $tmp $schema $seed $smoke 2>&1 | Tee-Object -FilePath $log
if ($LASTEXITCODE -ne 0) { throw "La validacion SQL fallo con codigo $LASTEXITCODE." }
Write-Host '[OK] SQL local validado.'
