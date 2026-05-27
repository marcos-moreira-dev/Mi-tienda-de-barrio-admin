@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0validate-shell-modular.ps1"
exit /b %ERRORLEVEL%
