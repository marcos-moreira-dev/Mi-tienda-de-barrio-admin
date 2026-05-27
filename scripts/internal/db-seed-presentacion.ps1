$ErrorActionPreference = "Stop"
$root = Resolve-Path "$PSScriptRoot\..\.."
$desktop = Join-Path $root "desktop"
$runtimeRoot = Join-Path $env:USERPROFILE ".mi-tienda-de-barrio-admin-presentacion"
$db = Join-Path $runtimeRoot "data\mi_tienda_de_barrio_admin.sqlite"
$backupDir = Join-Path $runtimeRoot "backups"
$schema = Join-Path $root "database\sql\migrations\V001__schema_3fn_oficial.sql"
$seedInicial = Join-Path $root "database\sql\seeds\V001__seed_inicial_cliente.sql"
$seedPresentacion = Join-Path $root "database\sql\seeds\V001__seed_presentacion.sql"

Write-Host "== Mi tienda de barrio admin :: seed presentacion =="
Write-Host "Base de PRESENTACION: $db"
Write-Host "No se toca la base real del cliente. Se usa carpeta separada de presentacion."
New-Item -ItemType Directory -Force -Path (Split-Path $db) | Out-Null
New-Item -ItemType Directory -Force -Path $backupDir | Out-Null
if (Test-Path $db) {
  $stamp = Get-Date -Format "yyyyMMdd_HHmmss"
  $backup = Join-Path $backupDir "pre_seed_presentacion_$stamp.sqlite"
  Copy-Item $db $backup -Force
  Write-Host "Respaldo previo: $backup"
}
Set-Location $desktop
mvn -q -DskipTests compile exec:java -Dexec.mainClass=com.marcosmoreira.mitiendadebarrio.admin.tools.LocalSqlScriptRunner "-Dmitienda.db.file=$db" "-Dmitienda.sql.file=$schema"
mvn -q -DskipTests compile exec:java -Dexec.mainClass=com.marcosmoreira.mitiendadebarrio.admin.tools.LocalSqlScriptRunner "-Dmitienda.db.file=$db" "-Dmitienda.sql.file=$seedInicial"
mvn -q -DskipTests compile exec:java -Dexec.mainClass=com.marcosmoreira.mitiendadebarrio.admin.tools.LocalSqlScriptRunner "-Dmitienda.db.file=$db" "-Dmitienda.sql.file=$seedPresentacion"
Write-Host "Datos de presentacion aplicados."
Write-Host "Para abrir esta base use: scripts\dev-desktop-presentacion.bat"
