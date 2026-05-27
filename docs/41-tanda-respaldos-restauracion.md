# Tanda 8 — Respaldos y restauración

## Objetivo

Permitir respaldo manual de la base SQLite y restauración guiada desde la aplicación.

## Alcance implementado

- Crear respaldo manual.
- Registrar metadatos del respaldo en SQLite.
- Calcular hash SHA-256.
- Listar respaldos recientes.
- Abrir carpeta de respaldos.
- Restaurar respaldo seleccionado creando antes un respaldo pre-restauración.

## Advertencia operativa

Después de restaurar, el usuario debe cerrar y abrir la aplicación para evitar operar sobre una conexión o estado visual anterior.

## Trazabilidad

JavaFX → RespaldoService → RespaldoRepository → copia física de archivo SQLite → registro en respaldo_sistema.
