# Limpieza de onboarding, scripts y runtime local

## Decisión

La carpeta `scripts` debe mostrar pocos comandos de uso diario. Los validadores específicos quedan en `scripts/internal` y son llamados por `test.bat`.

## Problema corregido

El arranque de escritorio podía usar una base vieja ubicada en el perfil de Windows, por ejemplo `.mi-tienda-de-barrio-admin`, y fallar porque esa base no tenía `schema_version`.

Ahora `dev-desktop.bat` usa por defecto un runtime local dentro del proyecto:

```text
.runtime/dev
```

La presentación usa:

```text
.runtime/presentacion
```

## Scripts visibles

- `test.bat`: prueba rápida integral.
- `scripts/dev-desktop.bat`: abre la app en modo desarrollo con runtime local del proyecto.
- `scripts/dev-desktop-presentacion.bat`: abre la app con runtime de presentación.
- `scripts/open-runtime-data.bat`: abre `.runtime/dev`.
- `scripts/reset-runtime-data.bat`: mueve `.runtime/dev` a `.runtime/_backups`.
- `scripts/release-preflight.bat`: revisión final de release.
- `scripts/package-release-local.bat`: empaqueta ZIP local.

## Regla

No se usa `Move-Item` con una ruta de ejemplo literal. Si hay que limpiar base de desarrollo, se ejecuta:

```powershell
.\scripts\reset-runtime-data.bat
```

Luego:

```powershell
.\scripts\dev-desktop.bat
```
