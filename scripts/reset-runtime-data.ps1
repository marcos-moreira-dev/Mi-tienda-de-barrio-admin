$ErrorActionPreference = "Stop"
$root = Resolve-Path "$PSScriptRoot\.."
$runtimeRoot = Join-Path $root ".runtime\dev"
$backupRoot = Join-Path $root ".runtime\_backups"
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
Write-Host "== MiTienda ERP Local :: limpiar runtime de desarrollo =="
Write-Host "Runtime: $runtimeRoot"
if (-not (Test-Path $runtimeRoot)) {
    Write-Host "No existe runtime local de desarrollo. No hay nada que mover."
    exit 0
}
New-Item -ItemType Directory -Force -Path $backupRoot | Out-Null
$destination = Join-Path $backupRoot "dev_$timestamp"
Move-Item -Path $runtimeRoot -Destination $destination
Write-Host "[OK] Runtime anterior movido a: $destination"
Write-Host "Al ejecutar .\scripts\dev-desktop.bat se creara una base limpia con la V001 consolidada."
