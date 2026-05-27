$ErrorActionPreference = 'Stop'
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = Resolve-Path (Join-Path $scriptDir '..\..')
$schema = Join-Path $root 'database\sql\migrations\V001__schema_erp_local_sqlite_consolidado.sql'
$seed = Join-Path $root 'database\sql\seeds\V001__seed_inicial_cliente.sql'
$smoke = Join-Path $root 'database\sql\checks\V001__smoke_check.sql'
$appBootstrap = Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\bootstrap\AppBootstrap.java'
$appContext = Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\bootstrap\AppContext.java'
$terceroService = Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\application\tercero\TerceroService.java'
$terceroRepo = Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\infrastructure\tercero\SqliteTerceroRepository.java'
$logDir = Join-Path $root '.diagnostics\logs'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$log = Join-Path $logDir "validate-terceros-locales_$timestamp.log"

Write-Host '== MiTienda :: validacion terceros locales =='
Write-Host "Log: $log"

$python = Get-Command python -ErrorAction SilentlyContinue
if (-not $python) { $python = Get-Command py -ErrorAction SilentlyContinue }
if (-not $python) { throw 'No se encontro Python para validar SQLite temporalmente.' }

$code = @'
import pathlib, sqlite3, sys
schema = pathlib.Path(sys.argv[1])
seed = pathlib.Path(sys.argv[2])
smoke = pathlib.Path(sys.argv[3])
app_bootstrap = pathlib.Path(sys.argv[4])
app_context = pathlib.Path(sys.argv[5])
tercero_service = pathlib.Path(sys.argv[6])
tercero_repo = pathlib.Path(sys.argv[7])
conn = sqlite3.connect(':memory:')
conn.execute('PRAGMA foreign_keys=ON')
conn.executescript(schema.read_text(encoding='utf-8'))
conn.executescript(seed.read_text(encoding='utf-8'))
conn.executescript(smoke.read_text(encoding='utf-8'))
required_tables = ['tercero', 'cliente_perfil', 'proveedor_perfil', 'tercero_contacto', 'tercero_direccion']
for table in required_tables:
    if not conn.execute("SELECT 1 FROM sqlite_master WHERE type='table' AND name=?", (table,)).fetchone():
        raise SystemExit(f'No existe tabla {table}.')
seed_counts = {
    'tercero': conn.execute('SELECT COUNT(*) FROM tercero').fetchone()[0],
    'cliente_perfil': conn.execute('SELECT COUNT(*) FROM cliente_perfil').fetchone()[0],
    'proveedor_perfil': conn.execute('SELECT COUNT(*) FROM proveedor_perfil').fetchone()[0],
}
if seed_counts['tercero'] < 2 or seed_counts['cliente_perfil'] < 1 or seed_counts['proveedor_perfil'] < 1:
    raise SystemExit('Seed de terceros incompleto: ' + repr(seed_counts))
cur = conn.cursor()
cur.execute("""
INSERT INTO tercero (tipo_tercero, tipo_identificacion, numero_identificacion, nombre_legal, nombre_comercial, telefono, correo)
VALUES ('NEGOCIO', 'RUC', '0999999999001', 'Proveedor de prueba S.A.', 'Proveedor prueba', '0999999999', 'compras@proveedor.test')
""")
tercero_id = cur.lastrowid
cur.execute("INSERT INTO proveedor_perfil (tercero_id, dias_credito, contacto_compras) VALUES (?, 15, 'Compras')", (tercero_id,))
cur.execute("INSERT INTO cliente_perfil (tercero_id, permite_fiado, limite_credito) VALUES (?, 1, 25.00)", (tercero_id,))
cur.execute("INSERT INTO tercero_contacto (tercero_id, nombre, telefono, principal) VALUES (?, 'Contacto prueba', '0999999998', 1)", (tercero_id,))
cur.execute("INSERT INTO tercero_direccion (tercero_id, direccion, referencia, principal) VALUES (?, 'Direccion prueba', 'Referencia prueba', 1)", (tercero_id,))
row = conn.execute("""
SELECT t.nombre_comercial, cp.permite_fiado, pp.dias_credito
FROM tercero t
JOIN cliente_perfil cp ON cp.tercero_id = t.id
JOIN proveedor_perfil pp ON pp.tercero_id = t.id
WHERE t.id = ?
""", (tercero_id,)).fetchone()
if row != ('Proveedor prueba', 1, 15):
    raise SystemExit('No se pudo leer tercero con perfiles: ' + repr(row))
errors = conn.execute('PRAGMA foreign_key_check').fetchall()
if errors:
    raise SystemExit('Errores de claves foraneas: ' + repr(errors))
checks = [
    (app_bootstrap, 'TerceroService'),
    (app_bootstrap, 'SqliteTerceroRepository'),
    (app_context, 'terceroService'),
    (tercero_service, 'marcarComoCliente'),
    (tercero_service, 'MARCAR_PROVEEDOR'),
    (tercero_repo, 'INSERT INTO tercero'),
    (tercero_repo, 'cliente_perfil'),
    (tercero_repo, 'proveedor_perfil'),
]
for path, token in checks:
    text = path.read_text(encoding='utf-8')
    if token not in text:
        raise SystemExit(f'No se encontro {token} en {path}')
print('OK terceros locales: tablas, seed, perfiles, contactos, direcciones, claves foraneas y wiring Java.')
'@
$tmp = Join-Path $env:TEMP "mitienda_terceros_validate_$timestamp.py"
$code | Set-Content -Path $tmp -Encoding UTF8
& $python.Source $tmp $schema $seed $smoke $appBootstrap $appContext $terceroService $terceroRepo 2>&1 | Tee-Object -FilePath $log
if ($LASTEXITCODE -ne 0) { throw "La validacion de terceros locales fallo con codigo $LASTEXITCODE." }
Write-Host '[OK] Terceros locales validados.'
