$ErrorActionPreference = "Stop"
$root = Resolve-Path "$PSScriptRoot\..\.."
$required = @(
  "README.md",
  "desktop/pom.xml",
  "desktop/src/main/java/com/marcosmoreira/mitiendadebarrio/admin/MiTiendaDeBarrioAdminApp.java",
  "database/sql/migrations/V001__schema_3fn_oficial.sql",
  "docs/28-tanda-core-embebido-1.md"
)
Write-Host "== Mi tienda de barrio admin :: validacion estructural =="
foreach ($item in $required) {
  if (-not (Test-Path (Join-Path $root $item))) {
    throw "Falta: $item"
  }
  Write-Host "OK: $item"
}
