# Orden de implementación UI + Core

## Pregunta resuelta

¿Conviene implementar primero módulos transversales de JavaFX o core embebido?

## Respuesta

Conviene avanzar ambos de forma mínima y coordinada. Primero se cierra la base transversal JavaFX junto con los contratos mínimos del core.

## Orden oficial

1. Proyecto JavaFX ejecutable.
2. Runtime local: data, backups, reports, images, logs, config, license.
3. Contratos del core: OperationResult, AppError, excepciones, PageResult.
4. Loading, login y shell principal.
5. Conexión SQLite y migraciones locales.
6. Primer módulo vertical: Configuración del negocio.
7. Productos.
8. Compras/Entradas.
9. Movimientos/Salidas.
10. Reportes y respaldos.
11. Licencia local.

## Razón

La UI necesita contratos mínimos para no acoplarse directamente a SQLite. El core necesita una UI mínima para probar flujos reales.
