@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0db-reset-presentacion.ps1"
endlocal
