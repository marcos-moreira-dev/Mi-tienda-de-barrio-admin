$ErrorActionPreference = 'Stop'
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = Resolve-Path (Join-Path $scriptDir '..\..')
$rootPath = $root.Path
$logDir = Join-Path $rootPath '.diagnostics\logs'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$log = Join-Path $logDir "release-preflight_$timestamp.log"

Write-Host '== MiTienda ERP Local :: preflight de release local =='
Write-Host "Raiz: $rootPath"
Write-Host "Log: $log"

$failures = New-Object System.Collections.Generic.List[string]
$warnings = New-Object System.Collections.Generic.List[string]

function Add-Failure([string] $message) {
    $script:failures.Add($message) | Out-Null
    Write-Host "[ERROR] $message"
}

function Add-Warning([string] $message) {
    $script:warnings.Add($message) | Out-Null
    Write-Host "[WARN] $message"
}

function Assert-File([string] $relativePath) {
    $path = Join-Path $rootPath $relativePath
    if (-not (Test-Path $path)) {
        Add-Failure "Falta archivo obligatorio: $relativePath"
    }
}

function Assert-Contains([string] $relativePath, [string] $pattern, [string] $message) {
    $path = Join-Path $rootPath $relativePath
    if (-not (Test-Path $path)) {
        Add-Failure "No se puede revisar archivo ausente: $relativePath"
        return
    }
    $content = Get-Content -Raw -Path $path -Encoding UTF8
    if ($content -notmatch [regex]::Escape($pattern)) {
        Add-Failure "$message. Falta: $pattern en $relativePath"
    }
}

function Compare-TextFile([string] $leftRelative, [string] $rightRelative) {
    $left = Join-Path $rootPath $leftRelative
    $right = Join-Path $rootPath $rightRelative
    if (-not (Test-Path $left)) { Add-Failure "Falta archivo: $leftRelative"; return }
    if (-not (Test-Path $right)) { Add-Failure "Falta archivo: $rightRelative"; return }
    $leftText = Get-Content -Raw -Path $left -Encoding UTF8
    $rightText = Get-Content -Raw -Path $right -Encoding UTF8
    if ($leftText -ne $rightText) {
        Add-Failure "Los archivos no estan sincronizados: $leftRelative vs $rightRelative"
    }
}

$requiredFiles = @(
    'README.md',
    'test.bat',
    'scripts\test.bat',
    'scripts\release-preflight.bat',
    'scripts\internal\release-preflight.ps1',
    'scripts\package-release-local.bat',
    'scripts\internal\package-release-local.ps1',
    'scripts\dev-desktop.bat',
    'scripts\dev-desktop.ps1',
    'scripts\dev-desktop-presentacion.bat',
    'scripts\dev-desktop-presentacion.ps1',
    'scripts\open-runtime-data.bat',
    'scripts\open-runtime-data.ps1',
    'scripts\reset-runtime-data.bat',
    'scripts\reset-runtime-data.ps1',
    'scripts\internal\validate-core-no-javafx.bat',
    'scripts\internal\validate-desktop.bat',
    'scripts\internal\validate-sql-local.bat',
    'scripts\internal\validate-migrator-local.bat',
    'scripts\internal\validate-shell-modular.bat',
    'scripts\internal\validate-usuarios-locales.bat',
    'scripts\internal\validate-auditoria-local.bat',
    'scripts\internal\validate-respaldos-seguros.bat',
    'scripts\internal\validate-terceros-locales.bat',
    'scripts\internal\validate-inventario-fuerte.bat',
    'scripts\internal\validate-compras-avanzadas.bat',
    'scripts\internal\validate-ventas-avanzadas.bat',
    'scripts\internal\validate-caja-gastos.bat',
    'scripts\internal\validate-cartera-local.bat',
    'scripts\internal\validate-reportes-pdf-formal.bat',
    'scripts\internal\validate-fiscalidad-preparada.bat',
    'scripts\internal\validate-contabilidad-basica.bat',
    'scripts\internal\validate-contabilidad-reglas.bat',
    'scripts\internal\validate-opcionales-minimos.bat',
    'scripts\internal\validate-ayuda-operativa.bat',
    'database\sql\migrations\V001__schema_erp_local_sqlite_consolidado.sql',
    'desktop\src\main\resources\db\migrations\V001__schema_erp_local_sqlite_consolidado.sql',
    'database\sql\seeds\V001__seed_inicial_cliente.sql',
    'desktop\src\main\resources\db\seeds\V001__seed_inicial_cliente.sql',
    'database\sql\checks\V001__smoke_check.sql',
    'desktop\src\main\resources\db\checks\V001__smoke_check.sql',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\bootstrap\AppBootstrap.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\bootstrap\AppContext.java',
    'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\infrastructure\sqlite\LocalDatabaseMigrator.java',
    'desktop\src\test\java\com\marcosmoreira\mitiendadebarrio\admin\bootstrap\AppBootstrapSmokeTest.java',
    'desktop\src\test\java\com\marcosmoreira\mitiendadebarrio\admin\core\infrastructure\sqlite\LocalDatabaseMigratorTest.java',
    'desktop\src\test\java\com\marcosmoreira\mitiendadebarrio\admin\core\infrastructure\runtime\RuntimePathsTest.java'
)
foreach ($file in $requiredFiles) { Assert-File $file }

Compare-TextFile 'database\sql\migrations\V001__schema_erp_local_sqlite_consolidado.sql' 'desktop\src\main\resources\db\migrations\V001__schema_erp_local_sqlite_consolidado.sql'
Compare-TextFile 'database\sql\seeds\V001__seed_inicial_cliente.sql' 'desktop\src\main\resources\db\seeds\V001__seed_inicial_cliente.sql'
Compare-TextFile 'database\sql\checks\V001__smoke_check.sql' 'desktop\src\main\resources\db\checks\V001__smoke_check.sql'

$sqlFiles = Get-ChildItem -Path (Join-Path $rootPath 'database\sql') -Recurse -File -Filter '*.sql'
foreach ($sqlFile in $sqlFiles) {
    $text = Get-Content -Raw -Path $sqlFile.FullName -Encoding UTF8
    if ($text -match '```') {
        Add-Failure "El SQL contiene cerca Markdown: $($sqlFile.FullName)"
    }
}

$schemaRelative = 'database\sql\migrations\V001__schema_erp_local_sqlite_consolidado.sql'
$schemaRequiredFragments = @(
    'CREATE TABLE IF NOT EXISTS schema_version',
    'CREATE TABLE IF NOT EXISTS usuario_local',
    'CREATE TABLE IF NOT EXISTS auditoria_evento',
    'CREATE TABLE IF NOT EXISTS tercero',
    'CREATE TABLE IF NOT EXISTS conteo_inventario',
    'CREATE TABLE IF NOT EXISTS cuenta_por_pagar',
    'CREATE TABLE IF NOT EXISTS venta_pago',
    'CREATE TABLE IF NOT EXISTS gasto_operativo',
    'CREATE TABLE IF NOT EXISTS documento_fiscal_preparado',
    'CREATE TABLE IF NOT EXISTS asiento_contable',
    'CREATE TABLE IF NOT EXISTS plantilla_asiento',
    'CREATE TABLE IF NOT EXISTS activo_negocio',
    'CREATE TABLE IF NOT EXISTS checklist_operativo'
)
foreach ($fragment in $schemaRequiredFragments) {
    Assert-Contains $schemaRelative $fragment 'La V001 consolidada no contiene una tabla esperada'
}

$migratorRelative = 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\core\infrastructure\sqlite\LocalDatabaseMigrator.java'
Assert-Contains $migratorRelative 'V001__schema_erp_local_sqlite_consolidado.sql' 'El migrador local debe apuntar a la V001 consolidada'
Assert-Contains $migratorRelative 'PRAGMA integrity_check' 'El migrador debe validar integridad SQLite'
Assert-Contains $migratorRelative 'PRAGMA foreign_key_check' 'El migrador debe validar claves foraneas'

$desktopTestRequiredFragments = @{
    'desktop\src\test\java\com\marcosmoreira\mitiendadebarrio\admin\bootstrap\AppBootstrapSmokeTest.java' = @('AppBootstrap.start()', 'assertNotNull')
    'desktop\src\test\java\com\marcosmoreira\mitiendadebarrio\admin\core\infrastructure\sqlite\LocalDatabaseMigratorTest.java' = @('LocalDatabaseMigrator', 'PRAGMA foreign_key_check')
    'desktop\src\test\java\com\marcosmoreira\mitiendadebarrio\admin\core\infrastructure\runtime\RuntimePathsTest.java' = @('RuntimePaths', 'assert')
}
foreach ($entry in $desktopTestRequiredFragments.GetEnumerator()) {
    foreach ($fragment in $entry.Value) {
        Assert-Contains $entry.Key $fragment 'El test Maven del desktop no contiene una verificacion esperada'
    }
}

$rootTest = Get-Content -Raw -Path (Join-Path $rootPath 'test.bat') -Encoding UTF8
$expectedCalls = @(
    'internal\validate-core-no-javafx.bat',
    'internal\validate-desktop.bat',
    'internal\validate-sql-local.bat',
    'internal\validate-migrator-local.bat',
    'internal\validate-shell-modular.bat',
    'internal\validate-usuarios-locales.bat',
    'internal\validate-auditoria-local.bat',
    'internal\validate-respaldos-seguros.bat',
    'internal\validate-terceros-locales.bat',
    'internal\validate-inventario-fuerte.bat',
    'internal\validate-compras-avanzadas.bat',
    'internal\validate-ventas-avanzadas.bat',
    'internal\validate-caja-gastos.bat',
    'internal\validate-cartera-local.bat',
    'internal\validate-reportes-pdf-formal.bat',
    'internal\validate-fiscalidad-preparada.bat',
    'internal\validate-contabilidad-basica.bat',
    'internal\validate-contabilidad-reglas.bat',
    'internal\validate-opcionales-minimos.bat',
    'internal\validate-ayuda-operativa.bat',
    'release-preflight.bat'
)
foreach ($call in $expectedCalls) {
    if ($rootTest -notmatch [regex]::Escape($call)) {
        Add-Failure "test.bat no ejecuta: $call"
    }
}

$forbiddenDirs = @('target', 'build', 'dist', '.gradle', 'node_modules')
foreach ($dirName in $forbiddenDirs) {
    $found = Get-ChildItem -Path $rootPath -Recurse -Directory -ErrorAction SilentlyContinue | Where-Object { $_.Name -eq $dirName -and $_.FullName -notmatch '\\.diagnostics\\' -and $_.FullName -notmatch '\\desktop\\target($|\\)' }
    foreach ($dir in $found) {
        Add-Warning "Directorio de build/dependencias presente: $($dir.FullName)"
    }
}

$forbiddenExtensions = @('*.class', '*.log')
foreach ($pattern in $forbiddenExtensions) {
    $foundFiles = Get-ChildItem -Path $rootPath -Recurse -File -Filter $pattern -ErrorAction SilentlyContinue | Where-Object { $_.FullName -notmatch '\\.diagnostics\\' -and $_.FullName -notmatch '\\desktop\\target\\' }
    foreach ($file in $foundFiles) {
        Add-Warning "Archivo generado presente fuera de .diagnostics: $($file.FullName)"
    }
}

$summary = @()
$summary += "MiTienda ERP Local - release preflight"
$summary += "Fecha: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
$summary += "Raiz: $rootPath"
$summary += "Errores: $($failures.Count)"
$summary += "Advertencias: $($warnings.Count)"
if ($failures.Count -gt 0) {
    $summary += ''
    $summary += 'Errores:'
    foreach ($failure in $failures) { $summary += "- $failure" }
}
if ($warnings.Count -gt 0) {
    $summary += ''
    $summary += 'Advertencias:'
    foreach ($warning in $warnings) { $summary += "- $warning" }
}
$summary | Set-Content -Path $log -Encoding UTF8

if ($failures.Count -gt 0) {
    throw "Preflight de release local fallo con $($failures.Count) error(es). Ver log: $log"
}

Write-Host "[OK] Preflight de release local completado. Advertencias: $($warnings.Count)."
Write-Host "Log: $log"
