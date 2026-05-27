@echo off
setlocal EnableExtensions
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0validate-contabilidad-basica.ps1"
exit /b %ERRORLEVEL%
