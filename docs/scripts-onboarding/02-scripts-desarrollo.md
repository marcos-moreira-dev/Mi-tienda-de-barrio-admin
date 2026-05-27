# Scripts de desarrollo

## Propósito

Definir scripts para levantar, validar y probar el proyecto en entorno local del desarrollador.

## Scripts previstos

- `dev-desktop.bat`: levantar la app JavaFX en modo desarrollo.
- `validate-docs.bat`: revisar estructura documental mínima.
- `validate-database.bat`: validar migraciones cuando existan.
- `package-local.bat`: construir distribución local cuando exista código.

## Reglas

- Todo script debe imprimir qué hace.
- Todo script debe escribir log en carpeta clara.
- No cerrar consola inmediatamente si hay error.
- No borrar datos sin confirmación explícita.
