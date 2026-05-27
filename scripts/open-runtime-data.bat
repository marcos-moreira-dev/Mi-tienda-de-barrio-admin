@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0open-runtime-data.ps1"
exit /b %ERRORLEVEL%
