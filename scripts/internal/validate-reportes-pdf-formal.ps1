$ErrorActionPreference = 'Stop'
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = Resolve-Path (Join-Path $scriptDir '..\..')
$schema = Join-Path $root 'database\sql\migrations\V001__schema_erp_local_sqlite_consolidado.sql'
$seed = Join-Path $root 'database\sql\seeds\V001__seed_inicial_cliente.sql'
$smoke = Join-Path $root 'database\sql\checks\V001__smoke_check.sql'
$logDir = Join-Path $root '.diagnostics\logs'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$log = Join-Path $logDir "validate-reportes-pdf-formal_$timestamp.log"

Write-Host '== MiTienda :: validacion reportes y PDF formal =='
Write-Host "Log: $log"

$enum = Get-Content (Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\domain\reporte\TipoReporteOperativo.java') -Raw
$repo = Get-Content (Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\infrastructure\reporte\SqliteReporteOperativoRepository.java') -Raw
$exporter = Get-Content (Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\application\reporte\ReporteExportService.java') -Raw
$service = Get-Content (Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\application\reporte\ReporteOperativoService.java') -Raw
$view = Get-Content (Join-Path $root 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\desktop\ui\screens\reportes\ReportesOperativosView.java') -Raw

$requiredReports = @(
    'CIERRE_CAJA_RECIENTE',
    'FIADO_PENDIENTE',
    'ABONOS_RECIENTES',
    'GASTOS_OPERATIVOS',
    'CUENTAS_POR_PAGAR'
)
foreach ($report in $requiredReports) {
    if ($enum -notmatch $report) { throw "Falta tipo de reporte en enum: $report" }
    if ($repo -notmatch $report) { throw "Repositorio no atiende el reporte: $report" }
}

foreach ($fragment in @('exportarPdfFormal', 'Reporte formal local', 'RESUMEN OPERATIVO', '%PDF-1.4')) {
    if ($exporter -notmatch [regex]::Escape($fragment)) { throw "ReporteExportService no contiene: $fragment" }
}
if ($service -notmatch 'exportarPdfFormal') { throw 'ReporteOperativoService no expone exportarPdfFormal.' }
if ($view -notmatch 'exportarPdfFormal') { throw 'ReportesOperativosView no usa exportarPdfFormal.' }
if ($view -match 'PDF básico') { throw 'La vista de reportes todavía menciona PDF básico.' }

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
# Datos mínimos para nuevos reportes
conn.execute("INSERT INTO categoria(nombre) VALUES('General Reportes')")
cat = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO unidad_medida(nombre,abreviatura) VALUES('Unidad Reportes','ur')")
uni = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO proveedor(nombre,telefono) VALUES('Proveedor Reportes','0991111111')")
prov = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO cliente_fiado(nombre,telefono) VALUES('Cliente Reportes','0981111111')")
cliente = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO producto(codigo_interno,nombre,categoria_id,unidad_medida_id,proveedor_principal_id,precio_compra_referencia,precio_venta,stock_actual,stock_minimo,stock_objetivo) VALUES('REP-001','Producto Reportes',?,?,?,5,8,2,5,10)", (cat, uni, prov))
producto = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO caja_diaria(fecha,saldo_inicial,total_ingresos,total_egresos,saldo_esperado,saldo_contado,diferencia,estado) VALUES('2026-05-26',100,50,20,130,128,-2,'CERRADA')")
caja = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO tipo_gasto(nombre) VALUES('Transporte Reportes')")
tipo_gasto = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO movimiento_caja(caja_diaria_id,tipo_movimiento,origen,monto,metodo_pago,descripcion) VALUES(?,?,?,?,?,?)", (caja,'EGRESO','GASTO_OPERATIVO',12,'EFECTIVO','Gasto reporte'))
mov_gasto = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO gasto_operativo(caja_diaria_id,tipo_gasto_id,movimiento_caja_id,monto,descripcion,forma_pago_codigo) VALUES(?,?,?,?,?,?)", (caja,tipo_gasto,mov_gasto,12,'Transporte de mercaderia','EFECTIVO'))
conn.execute("INSERT INTO venta_interna(cliente_fiado_id,total,metodo_pago,advertencia_tributaria_aceptada) VALUES(?,?,?,1)", (cliente,40,'FIADO'))
venta = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO detalle_venta_interna(venta_interna_id,producto_id,cantidad,precio_unitario,subtotal) VALUES(?,?,?,?,?)", (venta,producto,5,8,40))
conn.execute("INSERT INTO cuenta_por_cobrar(cliente_fiado_id,venta_interna_id,monto_original,saldo_pendiente,estado,observacion) VALUES(?,?,?,?,?,?)", (cliente,venta,40,25,'ABIERTA','Fiado de prueba'))
cuenta_cobrar = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO movimiento_caja(caja_diaria_id,tipo_movimiento,origen,monto,metodo_pago,descripcion) VALUES(?,?,?,?,?,?)", (caja,'INGRESO','ABONO_FIADO',15,'EFECTIVO','Abono reporte'))
mov_abono = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO abono(cuenta_por_cobrar_id,movimiento_caja_id,monto,metodo_pago,observacion) VALUES(?,?,?,?,?)", (cuenta_cobrar,mov_abono,15,'EFECTIVO','Abono prueba'))
conn.execute("INSERT INTO compra(proveedor_id,numero_comprobante,tipo_comprobante,total_estimado,estado) VALUES(?,?,?,?,?)", (prov,'COMP-REP','NOTA',60,'REGISTRADA'))
compra = conn.execute('SELECT last_insert_rowid()').fetchone()[0]
conn.execute("INSERT INTO detalle_compra(compra_id,producto_id,cantidad,costo_unitario,subtotal) VALUES(?,?,?,?,?)", (compra,producto,10,6,60))
conn.execute("INSERT INTO cuenta_por_pagar(compra_id,proveedor_id,monto_total,saldo_pendiente,estado,fecha_vencimiento) VALUES(?,?,?,?,?,?)", (compra,prov,60,35,'PARCIAL','2026-06-10'))
queries = {
    'cierre_caja': "SELECT fecha, saldo_inicial, total_ingresos, total_egresos, saldo_esperado, COALESCE(saldo_contado, ''), diferencia, estado FROM caja_diaria ORDER BY date(fecha) DESC, id DESC LIMIT 90",
    'fiado': "SELECT cf.nombre, COALESCE(cf.telefono, ''), cpc.fecha_apertura, cpc.monto_original, cpc.saldo_pendiente, cpc.estado, COALESCE(cpc.observacion, '') FROM cuenta_por_cobrar cpc JOIN cliente_fiado cf ON cf.id = cpc.cliente_fiado_id WHERE cpc.estado = 'ABIERTA' AND cpc.saldo_pendiente > 0 ORDER BY cpc.saldo_pendiente DESC, cpc.fecha_apertura ASC",
    'abonos': "SELECT a.fecha_abono, cf.nombre, a.monto, a.metodo_pago, COALESCE(a.observacion, ''), CASE WHEN a.movimiento_caja_id IS NULL THEN 'Sin caja' ELSE 'Con caja' END FROM abono a JOIN cuenta_por_cobrar cpc ON cpc.id = a.cuenta_por_cobrar_id JOIN cliente_fiado cf ON cf.id = cpc.cliente_fiado_id ORDER BY datetime(a.fecha_abono) DESC, a.id DESC LIMIT 100",
    'gastos': "SELECT go.fecha_gasto, tg.nombre, go.monto, go.forma_pago_codigo, go.descripcion, COALESCE(go.referencia, ''), CASE WHEN go.movimiento_caja_id IS NULL THEN 'Sin caja' ELSE 'Con caja' END FROM gasto_operativo go JOIN tipo_gasto tg ON tg.id = go.tipo_gasto_id ORDER BY datetime(go.fecha_gasto) DESC, go.id DESC LIMIT 100",
    'cuentas_pagar': "SELECT COALESCE(p.nombre, ''), c.fecha_compra, COALESCE(c.numero_comprobante, ''), cpp.monto_total, cpp.saldo_pendiente, COALESCE(cpp.fecha_vencimiento, ''), cpp.estado FROM cuenta_por_pagar cpp JOIN compra c ON c.id = cpp.compra_id LEFT JOIN proveedor p ON p.id = cpp.proveedor_id WHERE cpp.estado IN ('PENDIENTE','PARCIAL') AND cpp.saldo_pendiente > 0 ORDER BY date(COALESCE(cpp.fecha_vencimiento, c.fecha_compra)) ASC, cpp.saldo_pendiente DESC"
}
for name, sql in queries.items():
    rows = conn.execute(sql).fetchall()
    if not rows:
        raise SystemExit(f'Reporte sin filas en prueba: {name}')
errors = conn.execute('PRAGMA foreign_key_check').fetchall()
if errors:
    raise SystemExit(f'Errores FK: {errors}')
print('OK reportes nuevos: caja, fiado, abonos, gastos y cuentas por pagar.')
'@
$tmp = Join-Path $env:TEMP "mitienda_reportes_pdf_formal_$timestamp.py"
$code | Set-Content -Path $tmp -Encoding UTF8
& $python.Source $tmp $schema $seed $smoke 2>&1 | Tee-Object -FilePath $log
if ($LASTEXITCODE -ne 0) { throw "La validacion de reportes/PDF formal fallo con codigo $LASTEXITCODE." }
Write-Host '[OK] Reportes y PDF formal validados.'
