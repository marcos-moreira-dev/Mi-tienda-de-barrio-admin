$ErrorActionPreference = "Stop"
$root = Resolve-Path "$PSScriptRoot\.."
$desktop = Join-Path $root "desktop"
$runtimeRoot = Join-Path $root ".runtime\presentacion"
New-Item -ItemType Directory -Force -Path $runtimeRoot | Out-Null
Write-Host "== MiTienda ERP Local :: desktop presentacion =="
Write-Host "Runtime local de presentacion: $runtimeRoot"
Set-Location $desktop
$env:MITIENDA_RUNTIME_ROOT = $runtimeRoot
mvn "-Dmitienda.runtime.root=$runtimeRoot" javafx:run
exit $LASTEXITCODE "-Dmitienda.runtime.root=$runtimeRoot" "-Dmitienda.runtime.profile=presentacion"
