@echo off
setlocal
powershell -ExecutionPolicy Bypass -File "%~dp0validate-caja-gastos.ps1"
endlocal
