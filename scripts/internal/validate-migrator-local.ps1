$ErrorActionPreference = 'Stop'
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = Resolve-Path (Join-Path $scriptDir '..\..')
$java = Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\infrastructure\sqlite\LocalDatabaseMigrator.java'
$schema = Join-Path $root 'database\sql\migrations\V001__schema_erp_local_sqlite_consolidado.sql'
$seed = Join-Path $root 'database\sql\seeds\V001__seed_inicial_cliente.sql'
$smoke = Join-Path $root 'database\sql\checks\V001__smoke_check.sql'
$logDir = Join-Path $root '.diagnostics\logs'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$log = Join-Path $logDir "validate-migrator-local_$timestamp.log"

Write-Host '== MiTienda :: validacion migrador local =='
Write-Host "Raiz: $root"
Write-Host "Log: $log"

function Assert-Contains($path, $pattern, $message) {
    $content = Get-Content -Raw -Path $path -Encoding UTF8
    if ($content -notmatch [regex]::Escape($pattern)) {
        throw "$message. Falta: $pattern"
    }
}

Assert-Contains $java 'EXPECTED_VERSION = "V001"' 'El migrador debe declarar version esperada'
Assert-Contains $java 'EXPECTED_MIGRATION_NAME = "V001__schema_erp_local_sqlite_consolidado.sql"' 'El migrador debe apuntar a la V001 consolidada'
Assert-Contains $java 'hasSchemaVersionTable' 'El migrador debe verificar schema_version'
Assert-Contains $java 'hasUserTables' 'El migrador debe proteger bases antiguas sin schema_version'
Assert-Contains $java 'PRAGMA integrity_check' 'El migrador debe ejecutar integrity_check'
Assert-Contains $java 'PRAGMA foreign_key_check' 'El migrador debe ejecutar foreign_key_check'
Assert-Contains $schema 'CREATE TABLE IF NOT EXISTS schema_version' 'El schema debe crear schema_version'
Assert-Contains $seed "'V001'" 'El seed debe registrar V001'
Assert-Contains $smoke 'schema_version' 'El smoke check debe revisar schema_version'

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
conn.execute("""
INSERT INTO schema_version (id, version, nombre_migracion, estado, observacion)
VALUES (1, 'V001', 'V001__schema_erp_local_sqlite_consolidado.sql', 'APLICADA', 'Prueba del contrato del migrador local.')
ON CONFLICT(id) DO UPDATE SET version = excluded.version
""")
conn.executescript(seed.read_text(encoding='utf-8'))
conn.executescript(smoke.read_text(encoding='utf-8'))
version = conn.execute('SELECT version FROM schema_version WHERE id = 1').fetchone()[0]
if version != 'V001':
    raise SystemExit(f'Version inesperada: {version}')
integrity = [row[0] for row in conn.execute('PRAGMA integrity_check').fetchall()]
if integrity != ['ok']:
    raise SystemExit(f'Integrity check fallo: {integrity}')
fk = conn.execute('PRAGMA foreign_key_check').fetchall()
if fk:
    raise SystemExit(f'Foreign key check fallo: {fk}')
print('OK migrador local: contrato V001 + schema_version + integridad verificados.')
'@
$tmp = Join-Path $env:TEMP "mitienda_migrator_validate_$timestamp.py"
$code | Set-Content -Path $tmp -Encoding UTF8
& $python.Source $tmp $schema $seed $smoke 2>&1 | Tee-Object -FilePath $log
if ($LASTEXITCODE -ne 0) { throw "La validacion del migrador fallo con codigo $LASTEXITCODE." }

Write-Host '[OK] Migrador local validado.'
