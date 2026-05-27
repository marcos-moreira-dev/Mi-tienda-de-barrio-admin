$ErrorActionPreference = "Stop"
$root = Resolve-Path "$PSScriptRoot\..\.."
$desktop = Join-Path $root "desktop"
$db = Join-Path $env:USERPROFILE ".mi-tienda-de-barrio-admin\data\mi_tienda_de_barrio_admin.sqlite"
$schema = Join-Path $root "database\sql\migrations\V001__schema_3fn_oficial.sql"
$seed = Join-Path $root "database\sql\seeds\V001__seed_inicial_cliente.sql"

Write-Host "== Mi tienda de barrio admin :: seed inicial cliente =="
Write-Host "Base: $db"
Write-Host "Este script crea/actualiza solo estructura y datos mínimos de cliente real."
Set-Location $desktop
mvn -q -DskipTests compile exec:java -Dexec.mainClass=com.marcosmoreira.mitiendadebarrio.admin.tools.LocalSqlScriptRunner "-Dmitienda.db.file=$db" "-Dmitienda.sql.file=$schema"
mvn -q -DskipTests compile exec:java -Dexec.mainClass=com.marcosmoreira.mitiendadebarrio.admin.tools.LocalSqlScriptRunner "-Dmitienda.db.file=$db" "-Dmitienda.sql.file=$seed"
Write-Host "Seed inicial aplicado."
