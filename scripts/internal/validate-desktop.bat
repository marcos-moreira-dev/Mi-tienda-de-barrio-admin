@echo off
setlocal EnableExtensions
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0validate-desktop.ps1"
exit /b %ERRORLEVEL%
