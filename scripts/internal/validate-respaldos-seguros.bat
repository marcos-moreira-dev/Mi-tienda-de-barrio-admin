@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0validate-respaldos-seguros.ps1"
exit /b %ERRORLEVEL%
