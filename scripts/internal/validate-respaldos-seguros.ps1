$ErrorActionPreference = 'Stop'
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = Resolve-Path (Join-Path $scriptDir '..\..')
$logDir = Join-Path $root '.diagnostics\logs'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$log = Join-Path $logDir "validate-respaldos-seguros_$timestamp.log"

Write-Host '== MiTienda :: validacion respaldos seguros =='
Write-Host "Raiz: $root"
Write-Host "Log: $log"

$respaldoService = Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\application\respaldo\RespaldoService.java'
$respaldoRepository = Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\application\respaldo\RespaldoRepository.java'
$sqliteRespaldoRepository = Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\infrastructure\respaldo\SqliteRespaldoRepository.java'
$appBootstrap = Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\bootstrap\AppBootstrap.java'
$schema = Join-Path $root 'database\sql\migrations\V001__schema_erp_local_sqlite_consolidado.sql'
$seed = Join-Path $root 'database\sql\seeds\V001__seed_inicial_cliente.sql'
$smoke = Join-Path $root 'database\sql\checks\V001__smoke_check.sql'

foreach ($path in @($respaldoService, $respaldoRepository, $sqliteRespaldoRepository, $appBootstrap, $schema, $seed, $smoke)) {
    if (-not (Test-Path $path)) { throw "No existe archivo requerido: $path" }
}

$serviceText = Get-Content $respaldoService -Raw
$repoText = Get-Content $respaldoRepository -Raw
$sqliteRepoText = Get-Content $sqliteRespaldoRepository -Raw
$bootstrapText = Get-Content $appBootstrap -Raw

$requiredSnippets = @(
    'validarArchivoSqlite',
    'SQLITE_HEADER',
    'PRAGMA integrity_check',
    'PRAGMA foreign_key_check',
    'PRE_RESTAURACION',
    'marcarRestaurado',
    'AuditoriaService',
    'RESTAURAR_RESPALDO',
    'CREAR_RESPALDO'
)
foreach ($snippet in $requiredSnippets) {
    if (-not $serviceText.Contains($snippet)) { throw "RespaldoService no contiene: $snippet" }
}
if (-not $repoText.Contains('marcarRestaurado')) { throw 'RespaldoRepository no expone marcarRestaurado.' }
if (-not $sqliteRepoText.Contains("estado = 'RESTAURADO'")) { throw 'SqliteRespaldoRepository no marca estado RESTAURADO.' }
if (-not $bootstrapText.Contains('auditoriaService') -or -not $bootstrapText.Contains('new RespaldoService(')) { throw 'AppBootstrap no conecta respaldo con auditoria.' }

$python = Get-Command python -ErrorAction SilentlyContinue
if (-not $python) { $python = Get-Command py -ErrorAction SilentlyContinue }
if (-not $python) { throw 'No se encontro Python para validar SQLite temporalmente.' }

$code = @'
import pathlib, shutil, sqlite3, sys, tempfile
schema = pathlib.Path(sys.argv[1])
seed = pathlib.Path(sys.argv[2])
smoke = pathlib.Path(sys.argv[3])
with tempfile.TemporaryDirectory() as tmp:
    db = pathlib.Path(tmp) / 'mitienda.sqlite'
    conn = sqlite3.connect(db)
    conn.execute('PRAGMA foreign_keys=ON')
    conn.executescript(schema.read_text(encoding='utf-8'))
    conn.executescript(seed.read_text(encoding='utf-8'))
    conn.executescript(smoke.read_text(encoding='utf-8'))
    integrity = conn.execute('PRAGMA integrity_check').fetchall()
    fk = conn.execute('PRAGMA foreign_key_check').fetchall()
    conn.close()
    if integrity != [('ok',)]:
        print('INTEGRITY_ERRORS', integrity)
        raise SystemExit(1)
    if fk:
        print('FOREIGN_KEY_ERRORS', fk)
        raise SystemExit(1)
    backup = pathlib.Path(tmp) / 'backup.sqlite'
    shutil.copy2(db, backup)
    header = backup.read_bytes()[:16]
    if header != b'SQLite format 3\x00':
        print('BAD_SQLITE_HEADER', header)
        raise SystemExit(1)
    conn = sqlite3.connect(backup)
    check = conn.execute('PRAGMA integrity_check').fetchall()
    fk2 = conn.execute('PRAGMA foreign_key_check').fetchall()
    conn.close()
    if check != [('ok',)] or fk2:
        print('BAD_BACKUP_CHECKS', check, fk2)
        raise SystemExit(1)
print('OK respaldos seguros: schema, seed, smoke, cabecera SQLite, integrity_check y foreign_key_check validados.')
'@
$tmpPy = Join-Path $env:TEMP "mitienda_respaldos_seguros_$timestamp.py"
$code | Set-Content -Path $tmpPy -Encoding UTF8
& $python.Source $tmpPy $schema $seed $smoke 2>&1 | Tee-Object -FilePath $log
if ($LASTEXITCODE -ne 0) { throw "La validacion de respaldos seguros fallo con codigo $LASTEXITCODE." }

Write-Host '[OK] Respaldos seguros validados.'
