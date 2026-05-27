# Arranque y runtime de aplicación autocontenida

## Secuencia de arranque

1. Iniciar JavaFX.
2. Mostrar loading.
3. Crear/verificar carpetas runtime.
4. Cargar configuración local.
5. Verificar licencia.
6. Abrir conexión SQLite.
7. Aplicar/verificar migraciones.
8. Verificar integridad mínima.
9. Cargar sesión local.
10. Mostrar login o pantalla principal.

## Carpetas runtime

```text
runtime/
├── data/
├── backups/
├── reports/
├── images/products/
├── logs/
├── config/
└── license/
```

La pantalla de loading debe mostrar pasos entendibles: preparando carpetas, revisando base, verificando licencia y cargando configuración.
