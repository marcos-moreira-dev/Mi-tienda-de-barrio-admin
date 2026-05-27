$ErrorActionPreference = 'Stop'
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = Resolve-Path (Join-Path $scriptDir '..\..')
$schema = Join-Path $root 'database\sql\migrations\V001__schema_erp_local_sqlite_consolidado.sql'
$seed = Join-Path $root 'database\sql\seeds\V001__seed_inicial_cliente.sql'
$loginView = Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\desktop\ui\screens\login\LoginView.java'
$appBootstrap = Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\bootstrap\AppBootstrap.java'
$appContext = Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\bootstrap\AppContext.java'
$logDir = Join-Path $root '.diagnostics\logs'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$log = Join-Path $logDir "validate-usuarios-locales_$timestamp.log"

Write-Host '== MiTienda :: validacion usuarios locales =='
Write-Host "Log: $log"

$python = Get-Command python -ErrorAction SilentlyContinue
if (-not $python) { $python = Get-Command py -ErrorAction SilentlyContinue }
if (-not $python) { throw 'No se encontro Python para validar SQLite temporalmente.' }

$code = @'
import hashlib, pathlib, sqlite3, sys
schema = pathlib.Path(sys.argv[1])
seed = pathlib.Path(sys.argv[2])
login_view = pathlib.Path(sys.argv[3])
app_bootstrap = pathlib.Path(sys.argv[4])
app_context = pathlib.Path(sys.argv[5])
conn = sqlite3.connect(':memory:')
conn.execute('PRAGMA foreign_keys=ON')
conn.executescript(schema.read_text(encoding='utf-8'))
conn.executescript(seed.read_text(encoding='utf-8'))
required_tables = ['usuario_local','rol_local','permiso_local','rol_permiso_local','usuario_rol_local']
missing = [t for t in required_tables if not conn.execute("SELECT 1 FROM sqlite_master WHERE type='table' AND name=?", (t,)).fetchone()]
if missing:
    raise SystemExit('Faltan tablas: ' + ', '.join(missing))
admin = conn.execute("SELECT id,password_hash,password_salt,estado FROM usuario_local WHERE nombre_usuario='admin'").fetchone()
if not admin:
    raise SystemExit('No existe usuario admin inicial.')
admin_id, password_hash, salt, estado = admin
if estado != 'ACTIVO':
    raise SystemExit('El usuario admin no esta ACTIVO.')
expected = hashlib.sha256((salt + ':admin123456').encode('utf-8')).hexdigest()
if expected != password_hash:
    raise SystemExit('La clave inicial admin123456 no coincide con el hash sembrado.')
roles = [row[0] for row in conn.execute("SELECT r.codigo FROM rol_local r JOIN usuario_rol_local ur ON ur.rol_id=r.id WHERE ur.usuario_id=?", (admin_id,))]
if 'ADMIN_LOCAL' not in roles:
    raise SystemExit('El usuario admin no tiene rol ADMIN_LOCAL.')
perm_count = conn.execute("SELECT COUNT(*) FROM permiso_local").fetchone()[0]
if perm_count < 8:
    raise SystemExit('Hay muy pocos permisos locales sembrados.')
for path, token in [(login_view, 'usuarioLocalService().autenticar'), (app_bootstrap, 'UsuarioLocalService'), (app_context, 'usuarioLocalService')]:
    text = path.read_text(encoding='utf-8')
    if token not in text:
        raise SystemExit(f'No se encontro {token} en {path}')
errors = conn.execute('PRAGMA foreign_key_check').fetchall()
if errors:
    raise SystemExit('Errores de claves foraneas: ' + repr(errors))
print('OK usuarios locales: schema, seed, admin, rol, permisos y login conectado.')
'@
$tmp = Join-Path $env:TEMP "mitienda_usuarios_validate_$timestamp.py"
$code | Set-Content -Path $tmp -Encoding UTF8
& $python.Source $tmp $schema $seed $loginView $appBootstrap $appContext 2>&1 | Tee-Object -FilePath $log
if ($LASTEXITCODE -ne 0) { throw "La validacion de usuarios locales fallo con codigo $LASTEXITCODE." }
Write-Host '[OK] Usuarios locales validados.'
