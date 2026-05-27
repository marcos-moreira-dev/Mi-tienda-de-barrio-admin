# Hotfix — Maven tests desktop y runtime local

## Cambio

`test.bat` ahora ejecuta `mvn test` en el módulo `desktop` y deja la salida de Maven visible directamente en consola.

Además, el proyecto ahora incluye pruebas Maven reales en `desktop/src/test/java` para validar:

- resolución del runtime local;
- creación de la V001 consolidada por el migrador;
- arranque del contexto de aplicación sin lanzar JavaFX.

## Runtime desktop

`dev-desktop.bat` fuerza el runtime local del proyecto con:

- variable `MITIENDA_RUNTIME_ROOT`;
- propiedad `-Dmitienda.runtime.root=...`.

Si existe una base de desarrollo antigua sin `schema_version`, se mueve automáticamente a `.runtime/_backups` antes de iniciar la app.
