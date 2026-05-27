# Scripts de MiTienda ERP Local

Usa estos comandos desde la raíz del proyecto o desde `scripts`.

## Comandos normales

```powershell
.\test.bat
```

Ejecuta todas las validaciones rápidas, incluyendo compilación del core, `mvn test` del desktop JavaFX/Maven, SQL, módulos ERP locales y preflight.

```powershell
.\scripts\dev-desktop.bat
```

Abre la aplicación en modo desarrollo usando el runtime local del proyecto: `.runtime/dev`.
Si detecta una base vieja sin `schema_version`, la mueve automáticamente a `.runtime/_backups` y crea una base limpia.

```powershell
.\scripts\reset-runtime-data.bat
```

Mueve el runtime de desarrollo actual a `.runtime/_backups` para que la app cree una base limpia en el siguiente arranque.

```powershell
.\scripts\open-runtime-data.bat
```

Abre la carpeta `.runtime/dev`.

```powershell
.\scripts\release-preflight.bat
```

Revisa estructura, SQL, scripts, documentos y sincronía antes de release.

```powershell
.\scripts\package-release-local.bat
```

Genera un ZIP de release local en `.dist`.

## Scripts internos

Los validadores detallados viven en `scripts/internal`. No hace falta ejecutarlos uno por uno; `test.bat` los llama en orden.


## Nota de validación desktop

`test.bat` ejecuta `mvn test` dentro de `desktop` y muestra la salida de Maven directamente en consola. No necesitas revisar logs para copiar errores: copia la consola completa desde `== MiTienda :: validacion desktop JavaFX/Maven ==` si algo falla.

`dev-desktop.bat` fuerza el runtime local del proyecto mediante `MITIENDA_RUNTIME_ROOT` y `-Dmitienda.runtime.root=...`. Si encuentra una base de desarrollo vieja sin `schema_version`, la mueve automáticamente a `.runtime/_backups` antes de abrir la app.
