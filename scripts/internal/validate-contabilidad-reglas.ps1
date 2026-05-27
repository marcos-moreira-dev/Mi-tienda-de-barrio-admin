$ErrorActionPreference = 'Stop'
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = Resolve-Path (Join-Path $scriptDir '..\..')
$schema = Join-Path $root 'database\sql\migrations\V001__schema_erp_local_sqlite_consolidado.sql'
$seed = Join-Path $root 'database\sql\seeds\V001__seed_inicial_cliente.sql'
$smoke = Join-Path $root 'database\sql\checks\V001__smoke_check.sql'
$logDir = Join-Path $root '.diagnostics\logs'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$log = Join-Path $logDir "validate-contabilidad-reglas_$timestamp.log"

Write-Host '== MiTienda :: validacion plantillas y reglas contables =='
Write-Host "Raiz: $root"
Write-Host "Log: $log"

$requiredFiles = @(
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\contabilidad\PlantillaAsiento.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\contabilidad\PlantillaAsientoDetalle.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\contabilidad\ReglaContableEvento.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\contabilidad\CrearAsientoDesdePlantillaSolicitud.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\contabilidad\LadoPlantillaAsiento.java'
)
foreach ($relative in $requiredFiles) {
    $path = Join-Path $root $relative
    if (-not (Test-Path $path)) { throw "Falta archivo de plantillas/reglas contables: $relative" }
}

$service = Get-Content (Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\application\contabilidad\ContabilidadBasicaService.java') -Raw
$repository = Get-Content (Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\application\contabilidad\ContabilidadBasicaRepository.java') -Raw
$sqliteRepository = Get-Content (Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\infrastructure\contabilidad\SqliteContabilidadBasicaRepository.java') -Raw
if ($service -notmatch 'registrarAsientoDesdePlantilla') { throw 'ContabilidadBasicaService no registra asientos desde plantilla.' }
if ($repository -notmatch 'buscarReglaActivaPorEvento') { throw 'ContabilidadBasicaRepository no expone busqueda de regla por evento.' }
if ($sqliteRepository -notmatch 'listarPlantillasActivas') { throw 'SqliteContabilidadBasicaRepository no implementa plantillas.' }
if ($sqliteRepository -notmatch 'listarReglasActivas') { throw 'SqliteContabilidadBasicaRepository no implementa reglas.' }

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
for table in ['plantilla_asiento','plantilla_asiento_detalle','regla_contable_evento']:
    exists = conn.execute("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?", (table,)).fetchone()[0]
    if exists != 1:
        raise SystemExit(f'Falta tabla {table}')
if conn.execute('SELECT COUNT(*) FROM plantilla_asiento').fetchone()[0] < 8:
    raise SystemExit('Plantillas contables insuficientes')
if conn.execute('SELECT COUNT(*) FROM regla_contable_evento').fetchone()[0] < 8:
    raise SystemExit('Reglas contables insuficientes')
# Simular generacion de asiento desde regla VENTA_PAGADA.
importe = 25
plantilla_id = conn.execute("""
SELECT p.id
FROM regla_contable_evento r
JOIN plantilla_asiento p ON p.id = r.plantilla_id
WHERE r.evento_codigo='VENTA_PAGADA' AND r.activo=1 AND p.activo=1
""").fetchone()
if not plantilla_id:
    raise SystemExit('No existe regla activa VENTA_PAGADA')
plantilla_id = plantilla_id[0]
detalles = conn.execute("""
SELECT cuenta_id, linea, lado, descripcion
FROM plantilla_asiento_detalle
WHERE plantilla_id=?
ORDER BY linea
""", (plantilla_id,)).fetchall()
if len(detalles) != 2:
    raise SystemExit('La plantilla VENTA_PAGADA debe tener 2 lineas simples')
if {d[2] for d in detalles} != {'DEBE','HABER'}:
    raise SystemExit('La plantilla VENTA_PAGADA debe tener un DEBE y un HABER')
conn.execute("""
INSERT INTO asiento_contable(
    numero_asiento, tipo_diario_codigo, fecha_asiento, periodo_anio, periodo_mes,
    concepto, estado, origen_tipo, origen_id, total_debe, total_haber
) VALUES('ASI-PLANTILLA-00001','VENTAS','2026-05-26',2026,5,'Asiento generado desde plantilla','REGISTRADO','VENTA_INTERNA',1,?,?)
""", (importe, importe))
asiento_id = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
for cuenta_id, linea, lado, descripcion in detalles:
    debe = importe if lado == 'DEBE' else 0
    haber = importe if lado == 'HABER' else 0
    conn.execute("""
    INSERT INTO asiento_contable_detalle(asiento_id, cuenta_id, linea, descripcion, debe, haber)
    VALUES(?, ?, ?, ?, ?, ?)
    """, (asiento_id, cuenta_id, linea, descripcion, debe, haber))
total_debe = conn.execute('SELECT SUM(debe) FROM asiento_contable_detalle WHERE asiento_id=?', (asiento_id,)).fetchone()[0]
total_haber = conn.execute('SELECT SUM(haber) FROM asiento_contable_detalle WHERE asiento_id=?', (asiento_id,)).fetchone()[0]
if total_debe != total_haber or total_debe != importe:
    raise SystemExit('El asiento generado desde plantilla no cuadra')
errors = conn.execute('PRAGMA foreign_key_check').fetchall()
if errors:
    print('FOREIGN_KEY_ERRORS', errors)
    raise SystemExit(1)
if conn.execute('PRAGMA integrity_check').fetchone()[0] != 'ok':
    raise SystemExit('integrity_check fallo')
print('OK plantillas/reglas contables: semillas, regla VENTA_PAGADA y asiento cuadrado validos.')
print('Asiento generado desde plantilla id:', asiento_id)
'@
$tmp = Join-Path $env:TEMP "mitienda_contabilidad_reglas_validate_$timestamp.py"
$code | Set-Content -Path $tmp -Encoding UTF8
& $python.Source $tmp $schema $seed $smoke 2>&1 | Tee-Object -FilePath $log
if ($LASTEXITCODE -ne 0) { throw "La validacion de plantillas/reglas contables fallo con codigo $LASTEXITCODE." }
Write-Host '[OK] Plantillas y reglas contables validadas.'
