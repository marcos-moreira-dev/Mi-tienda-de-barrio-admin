$ErrorActionPreference = 'Stop'

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = Resolve-Path (Join-Path $scriptDir '..\..')
$rootPath = $root.Path
$src = Join-Path $rootPath 'desktop\src\main\java'
$out = Join-Path $rootPath '.diagnostics\build\core-no-javafx'
$logDir = Join-Path $rootPath '.diagnostics\logs'
New-Item -ItemType Directory -Force -Path $out | Out-Null
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$log = Join-Path $logDir "validate-core-no-javafx_$timestamp.log"

Write-Host '== MiTienda :: validacion core sin JavaFX =='
Write-Host "Raiz: $root"
Write-Host "Log: $log"


function Invoke-NativeForLog {
    param(
        [Parameter(Mandatory = $true)]
        [scriptblock] $Command,
        [Parameter(Mandatory = $true)]
        [string] $LogPath,
        [switch] $Append
    )

    $previousErrorActionPreference = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try {
        $output = & $Command 2>&1 | ForEach-Object { $_.ToString() }
        $exitCode = [int]$LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previousErrorActionPreference
    }

    if (-not $Append) {
        Set-Content -Path $LogPath -Value @() -Encoding UTF8
    }

    foreach ($line in $output) {
        Write-Host $line
        Add-Content -Path $LogPath -Value $line -Encoding UTF8
    }

    return $exitCode
}

$javaExitCode = Invoke-NativeForLog -Command { java -version } -LogPath $log
if ($javaExitCode -ne 0) {
    throw "No se pudo ejecutar java -version. Codigo: $javaExitCode."
}

$sources = Get-ChildItem -Path $src -Recurse -Filter '*.java' |
    Where-Object {
        $_.FullName -notmatch '\\desktop\\ui\\' -and
        $_.FullName -notmatch '\\MiTiendaDeBarrioAdminApp\.java$'
    } |
    ForEach-Object {
        $relative = $_.FullName.Substring($rootPath.Length + 1)
        # javac @argfile separa por espacios; por eso cada ruta va entre comillas.
        '"' + ($relative -replace '\\', '/') + '"'
    }

if (-not $sources -or $sources.Count -eq 0) {
    throw 'No se encontraron fuentes Java para validar.'
}

$argFile = Join-Path $out 'sources.txt'
$sources | Set-Content -Path $argFile -Encoding ASCII


Push-Location $rootPath
try {
    $javacExitCode = Invoke-NativeForLog -Command { javac --release 21 -encoding UTF-8 -d $out "@$argFile" } -LogPath $log -Append
} finally {
    Pop-Location
}
if ($javacExitCode -ne 0) {
    throw "La validacion core sin JavaFX fallo con codigo $javacExitCode."
}

Write-Host '[OK] Core/bootstrap/infrastructure sin JavaFX compila con javac --release 21.'
