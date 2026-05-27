@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0validate-usuarios-locales.ps1"
exit /b %ERRORLEVEL%
