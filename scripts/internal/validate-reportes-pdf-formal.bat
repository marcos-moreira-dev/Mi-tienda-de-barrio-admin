@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0validate-reportes-pdf-formal.ps1"
exit /b %ERRORLEVEL%
