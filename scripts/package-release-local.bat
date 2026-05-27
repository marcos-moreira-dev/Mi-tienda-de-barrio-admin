@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0internal\package-release-local.ps1"
exit /b %ERRORLEVEL%
