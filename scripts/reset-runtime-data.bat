@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0reset-runtime-data.ps1"
exit /b %ERRORLEVEL%
