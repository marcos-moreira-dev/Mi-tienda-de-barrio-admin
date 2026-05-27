@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0validate-fiscalidad-preparada.ps1"
exit /b %ERRORLEVEL%
