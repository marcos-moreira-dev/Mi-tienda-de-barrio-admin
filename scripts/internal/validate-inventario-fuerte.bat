@echo off
setlocal
set SCRIPT_DIR=%~dp0
powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT_DIR%validate-inventario-fuerte.ps1"
exit /b %ERRORLEVEL%
