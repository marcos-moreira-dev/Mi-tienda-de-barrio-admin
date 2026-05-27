@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0internal\release-preflight.ps1"
exit /b %ERRORLEVEL%
