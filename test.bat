@echo off
setlocal EnableExtensions
cd /d "%~dp0"
echo == MiTienda ERP Local :: test completo rapido ==
echo.

call scripts\internal\validate-core-no-javafx.bat || exit /b %ERRORLEVEL%
call scripts\internal\validate-desktop.bat || exit /b %ERRORLEVEL%
call scripts\internal\validate-sql-local.bat || exit /b %ERRORLEVEL%
call scripts\internal\validate-migrator-local.bat || exit /b %ERRORLEVEL%
call scripts\internal\validate-shell-modular.bat || exit /b %ERRORLEVEL%
call scripts\internal\validate-usuarios-locales.bat || exit /b %ERRORLEVEL%
call scripts\internal\validate-auditoria-local.bat || exit /b %ERRORLEVEL%
call scripts\internal\validate-respaldos-seguros.bat || exit /b %ERRORLEVEL%
call scripts\internal\validate-terceros-locales.bat || exit /b %ERRORLEVEL%
call scripts\internal\validate-inventario-fuerte.bat || exit /b %ERRORLEVEL%
call scripts\internal\validate-compras-avanzadas.bat || exit /b %ERRORLEVEL%
call scripts\internal\validate-ventas-avanzadas.bat || exit /b %ERRORLEVEL%
call scripts\internal\validate-caja-gastos.bat || exit /b %ERRORLEVEL%
call scripts\internal\validate-cartera-local.bat || exit /b %ERRORLEVEL%
call scripts\internal\validate-reportes-pdf-formal.bat || exit /b %ERRORLEVEL%
call scripts\internal\validate-fiscalidad-preparada.bat || exit /b %ERRORLEVEL%
call scripts\internal\validate-contabilidad-basica.bat || exit /b %ERRORLEVEL%
call scripts\internal\validate-contabilidad-reglas.bat || exit /b %ERRORLEVEL%
call scripts\internal\validate-opcionales-minimos.bat || exit /b %ERRORLEVEL%
call scripts\internal\validate-ayuda-operativa.bat || exit /b %ERRORLEVEL%
call scripts\release-preflight.bat || exit /b %ERRORLEVEL%

echo.
echo OK - Todas las validaciones rapidas pasaron.
exit /b 0
