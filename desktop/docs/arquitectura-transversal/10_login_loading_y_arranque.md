# Login, loading y arranque

## Login

Puede reutilizarse conceptualmente la ventana de login del sistema personal, adaptando:

- logo/nombre del producto;
- mensaje de licencia;
- usuario local si aplica;
- acceso a modo respaldo/soporte si la licencia está vencida.

## Loading

Debe mostrar pasos entendibles:

1. Cargando configuración local.
2. Verificando base de datos.
3. Aplicando migraciones pendientes.
4. Revisando licencia.
5. Preparando módulos.

## Errores de arranque

Deben traducirse a mensajes claros:

- no se pudo abrir la base;
- falta permiso de escritura;
- la licencia venció;
- se requiere respaldo antes de continuar;
- hay una migración fallida.
