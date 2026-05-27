$ErrorActionPreference = 'Stop'
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = Resolve-Path (Join-Path $scriptDir '..\..')
$schema = Join-Path $root 'database\sql\migrations\V001__schema_erp_local_sqlite_consolidado.sql'
$seed = Join-Path $root 'database\sql\seeds\V001__seed_inicial_cliente.sql'
$smoke = Join-Path $root 'database\sql\checks\V001__smoke_check.sql'
$logDir = Join-Path $root '.diagnostics\logs'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$log = Join-Path $logDir "validate-fiscalidad-preparada_$timestamp.log"

Write-Host '== MiTienda :: validacion fiscalidad preparada =='
Write-Host "Raiz: $root"
Write-Host "Log: $log"

$requiredFiles = @(
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\fiscalidad\DocumentoFiscalPreparado.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\fiscalidad\DocumentoFiscalPreparadoDetalle.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\fiscalidad\ImpuestoConfiguracion.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\application\fiscalidad\FiscalidadPreparadaService.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\infrastructure\fiscalidad\SqliteFiscalidadPreparadaRepository.java'
)
foreach ($relative in $requiredFiles) {
    $path = Join-Path $root $relative
    if (-not (Test-Path $path)) { throw "Falta archivo fiscal: $relative" }
}

$appContext = Get-Content (Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\bootstrap\AppContext.java') -Raw
$appBootstrap = Get-Content (Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\bootstrap\AppBootstrap.java') -Raw
if ($appContext -notmatch 'FiscalidadPreparadaService') { throw 'AppContext no expone FiscalidadPreparadaService.' }
if ($appBootstrap -notmatch 'SqliteFiscalidadPreparadaRepository') { throw 'AppBootstrap no conecta SqliteFiscalidadPreparadaRepository.' }

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
    'tipo_identificacion_local',
    'tipo_comprobante_local',
    'impuesto_configuracion',
    'documento_fiscal_preparado',
    'documento_fiscal_preparado_detalle',
]
for table in required:
    count = conn.execute("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?", (table,)).fetchone()[0]
    if count != 1:
        raise SystemExit(f'Falta tabla {table}')
if conn.execute("SELECT COUNT(*) FROM tipo_comprobante_local WHERE codigo='FACTURA_PREPARADA'").fetchone()[0] != 1:
    raise SystemExit('Falta FACTURA_PREPARADA')
if conn.execute("SELECT COUNT(*) FROM impuesto_configuracion WHERE codigo='SIN_IMPUESTO'").fetchone()[0] != 1:
    raise SystemExit('Falta impuesto SIN_IMPUESTO')
# Crear un tercero mínimo y un documento preparado de prueba.
conn.execute("""
INSERT INTO tercero(tipo_tercero,tipo_identificacion,numero_identificacion,nombre_legal,nombre_comercial)
VALUES('PERSONA_NATURAL','CEDULA','0000000000','Cliente fiscal preparado','Cliente fiscal preparado')
""")
tercero_id = conn.execute("SELECT last_insert_rowid()").fetchone()[0]
impuesto_id = conn.execute("SELECT id FROM impuesto_configuracion WHERE codigo='SIN_IMPUESTO'").fetchone()[0]
conn.execute("""
INSERT INTO documento_fiscal_preparado(
    tipo_comprobante_codigo, tercero_id, secuencia, subtotal, impuesto_total, total, advertencia_no_autorizado
) VALUES('FACTURA_PREPARADA', ?, 'FACT-PREP-000001', 10, 0, 10, 'Documento preparado. No reemplaza factura autorizada por el SRI.')
""", (tercero_id,))
doc_id = conn.execute("SELECT last_insert_rowid()").fetchone()[0]
conn.execute("""
INSERT INTO documento_fiscal_preparado_detalle(
    documento_id, descripcion, cantidad, precio_unitario, base_imponible, impuesto_id, valor_impuesto, total_linea
) VALUES(?, 'Producto de prueba', 2, 5, 10, ?, 0, 10)
""", (doc_id, impuesto_id))
errors = conn.execute('PRAGMA foreign_key_check').fetchall()
if errors:
    print('FOREIGN_KEY_ERRORS', errors)
    raise SystemExit(1)
if conn.execute('PRAGMA integrity_check').fetchone()[0] != 'ok':
    raise SystemExit('integrity_check fallo')
print('OK fiscalidad preparada: catalogos, impuesto, documento y detalle validos.')
print('Documento fiscal preparado id:', doc_id)
'@
$tmp = Join-Path $env:TEMP "mitienda_fiscalidad_validate_$timestamp.py"
$code | Set-Content -Path $tmp -Encoding UTF8
& $python.Source $tmp $schema $seed $smoke 2>&1 | Tee-Object -FilePath $log
if ($LASTEXITCODE -ne 0) { throw "La validacion fiscal fallo con codigo $LASTEXITCODE." }
Write-Host '[OK] Fiscalidad preparada validada.'
