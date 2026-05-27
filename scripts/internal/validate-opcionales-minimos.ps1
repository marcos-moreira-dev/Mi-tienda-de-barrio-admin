$ErrorActionPreference = 'Stop'
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = Resolve-Path (Join-Path $scriptDir '..\..')
$schema = Join-Path $root 'database\sql\migrations\V001__schema_erp_local_sqlite_consolidado.sql'
$seed = Join-Path $root 'database\sql\seeds\V001__seed_inicial_cliente.sql'
$smoke = Join-Path $root 'database\sql\checks\V001__smoke_check.sql'
$logDir = Join-Path $root '.diagnostics\logs'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$log = Join-Path $logDir "validate-opcionales-minimos_$timestamp.log"

Write-Host '== MiTienda :: validacion opcionales minimos =='
Write-Host "Raiz: $root"
Write-Host "Log: $log"

$requiredFiles = @(
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\opcional\ActivoNegocio.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\opcional\EmpleadoLocal.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\opcional\ChecklistOperativo.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\application\opcional\OpcionalesMinimosService.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\infrastructure\opcional\SqliteOpcionalesMinimosRepository.java'
)
foreach ($relative in $requiredFiles) {
    $path = Join-Path $root $relative
    if (-not (Test-Path $path)) { throw "Falta archivo de opcionales minimos: $relative" }
}

$appContext = Get-Content (Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\bootstrap\AppContext.java') -Raw
$appBootstrap = Get-Content (Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\bootstrap\AppBootstrap.java') -Raw
if ($appContext -notmatch 'OpcionalesMinimosService') { throw 'AppContext no expone OpcionalesMinimosService.' }
if ($appBootstrap -notmatch 'SqliteOpcionalesMinimosRepository') { throw 'AppBootstrap no conecta SqliteOpcionalesMinimosRepository.' }

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
    'tipo_activo_negocio','activo_negocio','cargo_empleado','empleado_local',
    'indicador_operativo','consulta_reporte_log','plantilla_importacion',
    'lote_importacion','error_importacion','checklist_operativo','checklist_item'
]
for table in required:
    exists = conn.execute("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?", (table,)).fetchone()[0]
    if exists != 1:
        raise SystemExit(f'Falta tabla {table}')
if conn.execute("SELECT COUNT(*) FROM tipo_activo_negocio").fetchone()[0] < 4:
    raise SystemExit('Tipos de activo insuficientes')
if conn.execute("SELECT COUNT(*) FROM cargo_empleado").fetchone()[0] < 3:
    raise SystemExit('Cargos insuficientes')
if conn.execute("SELECT COUNT(*) FROM indicador_operativo").fetchone()[0] < 4:
    raise SystemExit('Indicadores insuficientes')
if conn.execute("SELECT COUNT(*) FROM plantilla_importacion").fetchone()[0] < 3:
    raise SystemExit('Plantillas de importacion insuficientes')
if conn.execute("SELECT COUNT(*) FROM checklist_operativo").fetchone()[0] < 2:
    raise SystemExit('Checklists insuficientes')
if conn.execute("SELECT COUNT(*) FROM checklist_item").fetchone()[0] < 6:
    raise SystemExit('Items de checklist insuficientes')
# Insertar activo, empleado y lote con error para validar relaciones.
tipo_activo_id = conn.execute("SELECT id FROM tipo_activo_negocio WHERE codigo='COMPUTADORA'").fetchone()[0]
conn.execute("""
INSERT INTO activo_negocio(tipo_activo_id,codigo,nombre,valor_estimado,ubicacion,responsable)
VALUES(?,?,?,?,?,?)
""", (tipo_activo_id, 'ACT-PC-001', 'Laptop del negocio', 100, 'Caja', 'Administrador'))
cargo_id = conn.execute("SELECT id FROM cargo_empleado WHERE codigo='CAJERO'").fetchone()[0]
conn.execute("""
INSERT INTO empleado_local(cargo_id,nombre,telefono,estado)
VALUES(?,?,?,?)
""", (cargo_id, 'Empleado de prueba', '0999999999', 'ACTIVO'))
plantilla_id = conn.execute("SELECT id FROM plantilla_importacion WHERE codigo='PRODUCTOS_CSV'").fetchone()[0]
conn.execute("""
INSERT INTO lote_importacion(plantilla_id,tipo_importacion,archivo_origen,estado,total_filas,filas_validas,filas_con_error)
VALUES(?,?,?,?,?,?,?)
""", (plantilla_id, 'PRODUCTOS', 'productos.csv', 'VALIDADO', 2, 1, 1))
lote_id = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("""
INSERT INTO error_importacion(lote_importacion_id,numero_fila,campo,valor_original,mensaje,severidad)
VALUES(?,?,?,?,?,?)
""", (lote_id, 2, 'precio_venta', 'abc', 'Precio de venta invalido', 'ERROR'))
errors = conn.execute('PRAGMA foreign_key_check').fetchall()
if errors:
    print('FOREIGN_KEY_ERRORS', errors)
    raise SystemExit(1)
if conn.execute('PRAGMA integrity_check').fetchone()[0] != 'ok':
    raise SystemExit('integrity_check fallo')
print('OK opcionales minimos: activos, empleados, indicadores, importaciones y checklist validados.')
print('Activo:', conn.execute('SELECT nombre FROM activo_negocio WHERE codigo=?', ('ACT-PC-001',)).fetchone()[0])
print('Lote importacion:', lote_id)
'@
$tmp = Join-Path $env:TEMP "mitienda_opcionales_minimos_validate_$timestamp.py"
$code | Set-Content -Path $tmp -Encoding UTF8
& $python.Source $tmp $schema $seed $smoke 2>&1 | Tee-Object -FilePath $log
if ($LASTEXITCODE -ne 0) { throw "La validacion de opcionales minimos fallo con codigo $LASTEXITCODE." }
Write-Host '[OK] Opcionales minimos validados.'
