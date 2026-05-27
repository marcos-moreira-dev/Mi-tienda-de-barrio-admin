@echo off
setlocal EnableExtensions
cd /d "%~dp0.."
call test.bat
exit /b %ERRORLEVEL%
