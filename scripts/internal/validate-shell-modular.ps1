$ErrorActionPreference = 'Stop'

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = Resolve-Path (Join-Path $scriptDir '..\..')
$logDir = Join-Path $root '.diagnostics\logs'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$log = Join-Path $logDir "validate-shell-modular_$timestamp.log"

Write-Host '== MiTienda :: validacion shell modular =='
Write-Host "Raiz: $root"
Write-Host "Log: $log"

function Assert-File($relative) {
    $path = Join-Path $root $relative
    if (-not (Test-Path $path)) {
        throw "No existe archivo requerido: $relative"
    }
    return $path
}

function Assert-Contains($path, $pattern, $message) {
    $content = Get-Content -Raw -Path $path -Encoding UTF8
    if ($content -notmatch [regex]::Escape($pattern)) {
        throw "$message. Falta: $pattern"
    }
}

function Assert-NotContains($path, $pattern, $message) {
    $content = Get-Content -Raw -Path $path -Encoding UTF8
    if ($content -match [regex]::Escape($pattern)) {
        throw "$message. No deberia contener: $pattern"
    }
}

$descriptor = Assert-File 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\desktop\ui\shell\AppShellDescriptor.java'
$group = Assert-File 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\desktop\ui\shell\AppModuleGroupDescriptor.java'
$factory = Assert-File 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\desktop\ui\shell\ModuleViewFactory.java'
$policy = Assert-File 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\desktop\ui\shell\WriteModulePolicy.java'
$icons = Assert-File 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\desktop\ui\shell\ModuleIconResolver.java'
$shell = Assert-File 'desktop\src\main\java\com\marcosmoreira\mitiendadebarrio\admin\desktop\ui\screens\shell\MainShellView.java'

Assert-Contains $descriptor 'List<AppModuleGroupDescriptor> navigationGroups' 'El descriptor debe declarar grupos de navegacion'
Assert-Contains $descriptor 'findModule(String id)' 'El descriptor debe resolver modulos por id'
Assert-Contains $group 'record AppModuleGroupDescriptor' 'Debe existir record de grupos de navegacion'
Assert-Contains $factory 'public Node create(AppModuleDescriptor module)' 'La fabrica debe construir vistas por modulo'
Assert-Contains $policy 'requiresWriteAccess(String moduleId)' 'La politica debe decidir modulos de escritura'
Assert-Contains $icons 'iconFileFor(String moduleId)' 'El resolver debe mapear iconos'
Assert-Contains $shell 'ModuleViewFactory' 'MainShellView debe usar ModuleViewFactory'
Assert-Contains $shell 'WriteModulePolicy' 'MainShellView debe usar WriteModulePolicy'
Assert-Contains $shell 'ModuleIconResolver' 'MainShellView debe usar ModuleIconResolver'
Assert-Contains $shell 'shellDescriptor.navigationGroups()' 'MainShellView debe renderizar grupos desde descriptor'
Assert-Contains $shell 'moduleViewFactory.create(module)' 'MainShellView debe delegar construccion de vistas'
Assert-NotContains $shell 'case "home" -> new DashboardView' 'MainShellView no debe construir pantallas concretas con switch'
Assert-NotContains $shell 'private boolean isWriteModule' 'MainShellView no debe tener politica de escritura hardcodeada'

"OK shell modular: descriptor, grupos, fabrica, politica e iconos verificados." | Tee-Object -FilePath $log
Write-Host '[OK] Shell modular validado.'
