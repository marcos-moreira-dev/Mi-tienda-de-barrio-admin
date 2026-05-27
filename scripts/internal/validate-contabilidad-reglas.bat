@echo off
setlocal EnableExtensions
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0validate-contabilidad-reglas.ps1"
exit /b %ERRORLEVEL%
