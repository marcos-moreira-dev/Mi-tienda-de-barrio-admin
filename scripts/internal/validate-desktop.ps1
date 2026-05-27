$ErrorActionPreference = "Stop"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = Resolve-Path (Join-Path $scriptDir "..\..")
$desktopDir = Join-Path $root "desktop"

Write-Host "== MiTienda :: validacion desktop JavaFX/Maven =="
Write-Host "Raiz: $root"
Write-Host "Desktop: $desktopDir"
Write-Host "Requiere Eclipse Temurin JDK 21 y Maven disponible en PATH."
Write-Host "Ejecutando en consola: mvn test"

if (-not (Test-Path $desktopDir)) {
    throw "No existe la carpeta desktop: $desktopDir"
}

Push-Location $desktopDir
try {
    & mvn test
    $exitCode = $LASTEXITCODE
    if ($exitCode -ne 0) {
        throw "La validacion desktop Maven fallo con codigo $exitCode."
    }
    Write-Host "[OK] Desktop JavaFX compila y ejecuta los tests Maven."
}
finally {
    Pop-Location
}
