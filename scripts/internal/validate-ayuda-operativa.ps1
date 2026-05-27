$ErrorActionPreference = 'Stop'
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = Resolve-Path (Join-Path $scriptDir '..\..')
$schema = Join-Path $root 'database\sql\migrations\V001__schema_erp_local_sqlite_consolidado.sql'
$seed = Join-Path $root 'database\sql\seeds\V001__seed_inicial_cliente.sql'
$smoke = Join-Path $root 'database\sql\checks\V001__smoke_check.sql'
$logDir = Join-Path $root '.diagnostics\logs'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$log = Join-Path $logDir "validate-ayuda-operativa_$timestamp.log"

Write-Host '== MiTienda :: validacion ayuda operativa ampliada =='
Write-Host "Raiz: $root"
Write-Host "Log: $log"

$repo = Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\infrastructure\ayuda\SqliteAyudaContextualRepository.java'
$service = Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\application\ayuda\AyudaContextualService.java'
$view = Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\desktop\ui\screens\ayuda\AyudaContextualView.java'
foreach ($path in @($repo,$service,$view)) {
    if (-not (Test-Path $path)) { throw "Falta archivo de ayuda: $path" }
}
$repoText = Get-Content $repo -Raw
if ($repoText -notmatch 'ruta-diaria-operativa') { throw 'El repositorio de ayuda no contiene la ruta diaria fallback.' }
if ($repoText -notmatch 'documento-preparado') { throw 'El repositorio de ayuda no contiene fiscalidad preparada fallback.' }

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
required = [
    ('inicio','ruta_diaria_operativa'),
    ('inicio','ruta_semanal_control'),
    ('ventas-internas','venta_pagada_vs_fiada'),
    ('compras','compra_pagada_vs_credito'),
    ('caja','cierre_caja'),
    ('caja','gastos_operativos'),
    ('fiado','abonos'),
    ('cartera','cuentas_por_pagar'),
    ('inventario','conteo_ajuste'),
    ('reportes','lectura_reportes'),
    ('respaldos','respaldo_seguro'),
    ('licencia','modo_limitado_etico'),
    ('fiscalidad-preparada','documento_preparado'),
    ('contabilidad-basica','asiento_manual'),
    ('opciones-minimas','activos_empleados'),
    ('opciones-minimas','importacion_segura'),
]
for modulo, clave in required:
    found = conn.execute('SELECT COUNT(*) FROM ayuda_contextual WHERE modulo=? AND clave=? AND estado="ACTIVA"', (modulo, clave)).fetchone()[0]
    if found != 1:
        raise SystemExit(f'Falta ayuda activa: {modulo}/{clave}')
if conn.execute('SELECT COUNT(*) FROM ayuda_contextual WHERE estado="ACTIVA"').fetchone()[0] < 25:
    raise SystemExit('Hay muy pocas entradas de ayuda activa para el mini manual operativo')
if conn.execute("SELECT COUNT(*) FROM ayuda_contextual WHERE contenido LIKE '%No reemplaza comprobante autorizado por el SRI%'").fetchone()[0] < 1:
    raise SystemExit('Falta advertencia fiscal clara en ayuda')
if conn.execute("SELECT COUNT(*) FROM ayuda_contextual WHERE contenido LIKE '%debe y haber%'").fetchone()[0] < 1:
    raise SystemExit('Falta ayuda de contabilidad basica')
errors = conn.execute('PRAGMA foreign_key_check').fetchall()
if errors:
    print('FOREIGN_KEY_ERRORS', errors)
    raise SystemExit(1)
if conn.execute('PRAGMA integrity_check').fetchone()[0] != 'ok':
    raise SystemExit('integrity_check fallo')
print('OK ayuda operativa ampliada:', conn.execute('SELECT COUNT(*) FROM ayuda_contextual WHERE estado="ACTIVA"').fetchone()[0], 'entradas activas')
'@
$tmp = Join-Path $env:TEMP "mitienda_ayuda_operativa_validate_$timestamp.py"
$code | Set-Content -Path $tmp -Encoding UTF8
& $python.Source $tmp $schema $seed $smoke 2>&1 | Tee-Object -FilePath $log
if ($LASTEXITCODE -ne 0) { throw "La validacion de ayuda operativa fallo con codigo $LASTEXITCODE." }
Write-Host '[OK] Ayuda operativa ampliada validada.'
