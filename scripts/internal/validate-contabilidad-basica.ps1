$ErrorActionPreference = 'Stop'
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = Resolve-Path (Join-Path $scriptDir '..\..')
$schema = Join-Path $root 'database\sql\migrations\V001__schema_erp_local_sqlite_consolidado.sql'
$seed = Join-Path $root 'database\sql\seeds\V001__seed_inicial_cliente.sql'
$smoke = Join-Path $root 'database\sql\checks\V001__smoke_check.sql'
$logDir = Join-Path $root '.diagnostics\logs'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$log = Join-Path $logDir "validate-contabilidad-basica_$timestamp.log"

Write-Host '== MiTienda :: validacion contabilidad basica =='
Write-Host "Raiz: $root"
Write-Host "Log: $log"

$requiredFiles = @(
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\contabilidad\AsientoContable.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\contabilidad\AsientoContableDetalle.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\contabilidad\CuentaContable.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\application\contabilidad\ContabilidadBasicaService.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\application\contabilidad\ContabilidadBasicaRepository.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\infrastructure\contabilidad\SqliteContabilidadBasicaRepository.java'
)
foreach ($relative in $requiredFiles) {
    $path = Join-Path $root $relative
    if (-not (Test-Path $path)) { throw "Falta archivo de contabilidad: $relative" }
}

$appContext = Get-Content (Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\bootstrap\AppContext.java') -Raw
$appBootstrap = Get-Content (Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\bootstrap\AppBootstrap.java') -Raw
if ($appContext -notmatch 'ContabilidadBasicaService') { throw 'AppContext no expone ContabilidadBasicaService.' }
if ($appBootstrap -notmatch 'SqliteContabilidadBasicaRepository') { throw 'AppBootstrap no conecta SqliteContabilidadBasicaRepository.' }

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
    'tipo_cuenta_contable', 'cuenta_contable', 'tipo_diario_contable',
    'asiento_contable', 'asiento_contable_detalle', 'plantilla_asiento',
    'plantilla_asiento_detalle', 'regla_contable_evento'
]
for table in required:
    count = conn.execute("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?", (table,)).fetchone()[0]
    if count != 1:
        raise SystemExit(f'Falta tabla {table}')
if conn.execute("SELECT COUNT(*) FROM cuenta_contable").fetchone()[0] < 6:
    raise SystemExit('Plan de cuentas base insuficiente')
if conn.execute("SELECT COUNT(*) FROM tipo_diario_contable WHERE codigo='GENERAL'").fetchone()[0] != 1:
    raise SystemExit('Falta diario GENERAL')
# Registrar asiento cuadrado.
caja = conn.execute("SELECT id FROM cuenta_contable WHERE codigo='1.1.01'").fetchone()[0]
ingresos = conn.execute("SELECT id FROM cuenta_contable WHERE codigo='4.1.01'").fetchone()[0]
conn.execute("""
INSERT INTO asiento_contable(
    numero_asiento, tipo_diario_codigo, fecha_asiento, periodo_anio, periodo_mes,
    concepto, estado, total_debe, total_haber
) VALUES('ASI-TEST-00001','GENERAL','2026-05-26',2026,5,'Asiento de prueba cuadrado','REGISTRADO',10,10)
""")
asiento_id = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("""
INSERT INTO asiento_contable_detalle(asiento_id, cuenta_id, linea, descripcion, debe, haber)
VALUES(?, ?, 1, 'Caja', 10, 0)
""", (asiento_id, caja))
conn.execute("""
INSERT INTO asiento_contable_detalle(asiento_id, cuenta_id, linea, descripcion, debe, haber)
VALUES(?, ?, 2, 'Ingreso', 0, 10)
""", (asiento_id, ingresos))
# Probar que un asiento descuadrado sea rechazado por CHECK de cabecera.
try:
    conn.execute("""
    INSERT INTO asiento_contable(
        numero_asiento, tipo_diario_codigo, fecha_asiento, periodo_anio, periodo_mes,
        concepto, estado, total_debe, total_haber
    ) VALUES('ASI-TEST-MALO','GENERAL','2026-05-26',2026,5,'Asiento descuadrado','REGISTRADO',10,9)
    """)
    raise SystemExit('La base acepto un asiento descuadrado')
except sqlite3.IntegrityError:
    pass
# Probar que una linea no pueda tener debe y haber al mismo tiempo.
try:
    conn.execute("""
    INSERT INTO asiento_contable_detalle(asiento_id, cuenta_id, linea, descripcion, debe, haber)
    VALUES(?, ?, 3, 'Linea invalida', 1, 1)
    """, (asiento_id, caja))
    raise SystemExit('La base acepto una linea con debe y haber al mismo tiempo')
except sqlite3.IntegrityError:
    pass
errors = conn.execute('PRAGMA foreign_key_check').fetchall()
if errors:
    print('FOREIGN_KEY_ERRORS', errors)
    raise SystemExit(1)
if conn.execute('PRAGMA integrity_check').fetchone()[0] != 'ok':
    raise SystemExit('integrity_check fallo')
print('OK contabilidad basica: tablas, plan de cuentas, asiento cuadrado y restricciones validas.')
print('Asiento de prueba id:', asiento_id)
'@
$tmp = Join-Path $env:TEMP "mitienda_contabilidad_validate_$timestamp.py"
$code | Set-Content -Path $tmp -Encoding UTF8
& $python.Source $tmp $schema $seed $smoke 2>&1 | Tee-Object -FilePath $log
if ($LASTEXITCODE -ne 0) { throw "La validacion de contabilidad fallo con codigo $LASTEXITCODE." }
Write-Host '[OK] Contabilidad basica validada.'
