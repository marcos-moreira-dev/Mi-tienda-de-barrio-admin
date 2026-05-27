@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0validate-migrator-local.ps1"
exit /b %ERRORLEVEL%
