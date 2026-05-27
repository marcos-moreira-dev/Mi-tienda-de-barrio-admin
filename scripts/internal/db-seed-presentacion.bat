@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0db-seed-presentacion.ps1"
endlocal
