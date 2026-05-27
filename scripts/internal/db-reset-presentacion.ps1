$ErrorActionPreference = "Stop"
$root = Resolve-Path "$PSScriptRoot\..\.."
$desktop = Join-Path $root "desktop"
$runtimeRoot = Join-Path $env:USERPROFILE ".mi-tienda-de-barrio-admin-presentacion"
$db = Join-Path $runtimeRoot "data\mi_tienda_de_barrio_admin.sqlite"
$backupDir = Join-Path $runtimeRoot "backups"
$reset = Join-Path $root "database\sql\seeds\V001__reset_presentacion.sql"

Write-Host "== Mi tienda de barrio admin :: reset presentacion =="
Write-Host "Base de PRESENTACION: $db"
if (-not (Test-Path $db)) { throw "No existe la base de presentacion. Ejecute primero scripts\db-seed-presentacion.bat" }
Write-Host "Este script elimina SOLO datos inventados identificados como presentacion."
$confirm = Read-Host "Escriba PRESENTACION para continuar"
if ($confirm -ne "PRESENTACION") { Write-Host "Operacion cancelada."; exit 0 }
New-Item -ItemType Directory -Force -Path $backupDir | Out-Null
$stamp = Get-Date -Format "yyyyMMdd_HHmmss"
$backup = Join-Path $backupDir "pre_reset_presentacion_$stamp.sqlite"
Copy-Item $db $backup -Force
Write-Host "Respaldo previo: $backup"
Set-Location $desktop
mvn -q -DskipTests compile exec:java -Dexec.mainClass=com.marcosmoreira.mitiendadebarrio.admin.tools.LocalSqlScriptRunner "-Dmitienda.db.file=$db" "-Dmitienda.sql.file=$reset"
Write-Host "Reset de presentacion aplicado."
