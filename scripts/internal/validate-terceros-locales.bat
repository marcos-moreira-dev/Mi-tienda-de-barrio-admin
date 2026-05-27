@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0validate-terceros-locales.ps1"
exit /b %ERRORLEVEL%
