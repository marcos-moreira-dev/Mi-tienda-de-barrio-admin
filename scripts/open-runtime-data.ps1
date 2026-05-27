$ErrorActionPreference = "Stop"
$root = Resolve-Path "$PSScriptRoot\.."
$runtimeRoot = Join-Path $root ".runtime\dev"
New-Item -ItemType Directory -Force -Path $runtimeRoot | Out-Null
Write-Host "Abriendo runtime local del proyecto: $runtimeRoot"
explorer $runtimeRoot
