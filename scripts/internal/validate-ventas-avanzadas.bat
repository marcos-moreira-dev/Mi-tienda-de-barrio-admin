@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0validate-ventas-avanzadas.ps1"
exit /b %ERRORLEVEL%
