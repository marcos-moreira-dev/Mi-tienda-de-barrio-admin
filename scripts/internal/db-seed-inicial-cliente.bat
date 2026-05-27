@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0db-seed-inicial-cliente.ps1"
endlocal
