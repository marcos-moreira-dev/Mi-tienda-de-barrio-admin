$ErrorActionPreference = 'Stop'
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = Resolve-Path (Join-Path $scriptDir '..\..')
$rootPath = $root.Path
$dist = Join-Path $rootPath '.dist'
New-Item -ItemType Directory -Force -Path $dist | Out-Null
$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$zipPath = Join-Path $dist "MiTienda-ERP-Local-release_$timestamp.zip"

Write-Host '== MiTienda ERP Local :: empaquetado de release local =='
Write-Host "Raiz: $rootPath"
Write-Host "Salida: $zipPath"

& (Join-Path $scriptDir 'release-preflight.ps1')

$staging = Join-Path $env:TEMP "mitienda_release_$timestamp"
if (Test-Path $staging) { Remove-Item -Recurse -Force $staging }
New-Item -ItemType Directory -Force -Path $staging | Out-Null

$excludeNames = @('.git', '.diagnostics', '.dist', '.runtime', 'target', 'build', 'dist', 'node_modules', '.gradle')
Get-ChildItem -Path $rootPath -Force | Where-Object { $excludeNames -notcontains $_.Name } | ForEach-Object {
    $destination = Join-Path $staging $_.Name
    if ($_.PSIsContainer) {
        Copy-Item -Path $_.FullName -Destination $destination -Recurse -Force
    } else {
        Copy-Item -Path $_.FullName -Destination $destination -Force
    }
}

if (Test-Path $zipPath) { Remove-Item -Force $zipPath }
Compress-Archive -Path (Join-Path $staging '*') -DestinationPath $zipPath -Force
Remove-Item -Recurse -Force $staging
Write-Host "[OK] Release local empaquetado: $zipPath"
