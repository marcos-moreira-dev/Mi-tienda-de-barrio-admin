$ErrorActionPreference = "Stop"
$root = Resolve-Path "$PSScriptRoot\.."
$desktop = Join-Path $root "desktop"
$runtimeRoot = Join-Path $root ".runtime\dev"
$backupRoot = Join-Path $root ".runtime\_backups"
$db = Join-Path $runtimeRoot "data\mi_tienda_de_barrio_admin.sqlite"

function Test-SqliteHasSchemaVersion([string]$dbPath) {
    if (-not (Test-Path $dbPath)) { return $true }
    try {
        $tmp = [System.IO.Path]::GetTempFileName()
        $py = @"
import sqlite3, sys
path = sys.argv[1]
con = sqlite3.connect(path)
try:
    row = con.execute("SELECT name FROM sqlite_master WHERE type='table' AND name='schema_version'").fetchone()
    sys.exit(0 if row else 2)
finally:
    con.close()
"@
        Set-Content -Path $tmp -Value $py -Encoding UTF8
        & python $tmp $dbPath | Out-Null
        $code = $LASTEXITCODE
        Remove-Item $tmp -Force -ErrorAction SilentlyContinue
        return ($code -eq 0)
    } catch {
        return $false
    }
}

function Move-IncompatibleRuntimeIfNeeded() {
    if ((Test-Path $db) -and -not (Test-SqliteHasSchemaVersion $db)) {
        New-Item -ItemType Directory -Force -Path $backupRoot | Out-Null
        $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
        $destination = Join-Path $backupRoot "dev_incompatible_$timestamp"
        Write-Host "[ADVERTENCIA] Se encontro una base de desarrollo antigua sin schema_version."
        Write-Host "Se movera el runtime anterior a: $destination"
        Move-Item -Path $runtimeRoot -Destination $destination -Force
    }
}

Write-Host "== MiTienda ERP Local :: desktop dev =="
Write-Host "Runtime local del proyecto: $runtimeRoot"
Write-Host "Para limpiar/recrear la base de desarrollo: .\scripts\reset-runtime-data.bat"

Move-IncompatibleRuntimeIfNeeded
New-Item -ItemType Directory -Force -Path $runtimeRoot | Out-Null

$env:MITIENDA_RUNTIME_ROOT = $runtimeRoot
Set-Location $desktop
mvn "-Dmitienda.runtime.root=$runtimeRoot" javafx:run
exit $LASTEXITCODE
