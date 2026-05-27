$ErrorActionPreference = 'Stop'
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = Resolve-Path (Join-Path $scriptDir '..\..')
$schema = Join-Path $root 'database\sql\migrations\V001__schema_erp_local_sqlite_consolidado.sql'
$seed = Join-Path $root 'database\sql\seeds\V001__seed_inicial_cliente.sql'
$appBootstrap = Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\bootstrap\AppBootstrap.java'
$appContext = Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\bootstrap\AppContext.java'
$usuarioService = Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\application\seguridad\UsuarioLocalService.java'
$auditoriaService = Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\application\auditoria\AuditoriaService.java'
$auditoriaRepo = Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\infrastructure\auditoria\SqliteAuditoriaRepository.java'
$logDir = Join-Path $root '.diagnostics\logs'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$log = Join-Path $logDir "validate-auditoria-local_$timestamp.log"

Write-Host '== MiTienda :: validacion auditoria local =='
Write-Host "Log: $log"

$python = Get-Command python -ErrorAction SilentlyContinue
if (-not $python) { $python = Get-Command py -ErrorAction SilentlyContinue }
if (-not $python) { throw 'No se encontro Python para validar SQLite temporalmente.' }

$code = @'
import pathlib, sqlite3, sys
schema = pathlib.Path(sys.argv[1])
seed = pathlib.Path(sys.argv[2])
app_bootstrap = pathlib.Path(sys.argv[3])
app_context = pathlib.Path(sys.argv[4])
usuario_service = pathlib.Path(sys.argv[5])
auditoria_service = pathlib.Path(sys.argv[6])
auditoria_repo = pathlib.Path(sys.argv[7])
conn = sqlite3.connect(':memory:')
conn.execute('PRAGMA foreign_keys=ON')
conn.executescript(schema.read_text(encoding='utf-8'))
conn.executescript(seed.read_text(encoding='utf-8'))
if not conn.execute("SELECT 1 FROM sqlite_master WHERE type='table' AND name='auditoria_evento'").fetchone():
    raise SystemExit('No existe tabla auditoria_evento.')
admin_id = conn.execute("SELECT id FROM usuario_local WHERE nombre_usuario='admin'").fetchone()[0]
conn.execute("""
INSERT INTO auditoria_evento (usuario_id, modulo, accion, entidad, entidad_id, resumen, detalle_json, resultado)
VALUES (?, 'Prueba', 'VALIDACION_AUDITORIA', 'usuario_local', ?, 'Evento de auditoria local de prueba', '{"origen":"script"}', 'OK')
""", (admin_id, admin_id))
row = conn.execute("SELECT modulo, accion, resultado FROM auditoria_evento WHERE accion='VALIDACION_AUDITORIA'").fetchone()
if row != ('Prueba', 'VALIDACION_AUDITORIA', 'OK'):
    raise SystemExit('No se pudo insertar/leer evento de auditoria.')
errors = conn.execute('PRAGMA foreign_key_check').fetchall()
if errors:
    raise SystemExit('Errores de claves foraneas: ' + repr(errors))
checks = [
    (app_bootstrap, 'AuditoriaService'),
    (app_bootstrap, 'SqliteAuditoriaRepository'),
    (app_context, 'auditoriaService'),
    (usuario_service, 'registrarExito'),
    (usuario_service, 'LOGIN_CORRECTO'),
    (usuario_service, 'LOGIN_FALLIDO'),
    (auditoria_service, 'registrarAdvertencia'),
    (auditoria_repo, 'INSERT INTO auditoria_evento'),
]
for path, token in checks:
    text = path.read_text(encoding='utf-8')
    if token not in text:
        raise SystemExit(f'No se encontro {token} en {path}')
print('OK auditoria local: tabla, insercion, claves foraneas, bootstrap y login auditado.')
'@
$tmp = Join-Path $env:TEMP "mitienda_auditoria_validate_$timestamp.py"
$code | Set-Content -Path $tmp -Encoding UTF8
& $python.Source $tmp $schema $seed $appBootstrap $appContext $usuarioService $auditoriaService $auditoriaRepo 2>&1 | Tee-Object -FilePath $log
if ($LASTEXITCODE -ne 0) { throw "La validacion de auditoria local fallo con codigo $LASTEXITCODE." }
Write-Host '[OK] Auditoria local validada.'
