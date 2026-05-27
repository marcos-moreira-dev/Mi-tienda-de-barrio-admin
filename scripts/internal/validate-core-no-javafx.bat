@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0validate-core-no-javafx.ps1"
exit /b %ERRORLEVEL%
