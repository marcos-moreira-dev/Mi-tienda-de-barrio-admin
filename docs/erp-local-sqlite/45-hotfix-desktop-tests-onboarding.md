# Hotfix — tests desktop y onboarding limpio

## Cambios

Se corrigió `test.bat` para incluir validación de desktop JavaFX/Maven mediante `scripts/internal/validate-desktop.bat`.

También se corrigió `validate-contabilidad-basica.bat`, que todavía apuntaba a una ruta antigua después de mover validadores a `scripts/internal`.

## Runtime de desarrollo

`dev-desktop.bat` usa `.runtime/dev` como runtime local del proyecto.

Si detecta una base SQLite antigua sin `schema_version`, la mueve automáticamente a `.runtime/_backups/dev_incompatible_<timestamp>` y deja que la aplicación cree una base limpia con la V001 consolidada.

## Onboarding

La carpeta `scripts` queda como entrada humana. Los validadores detallados quedan en `scripts/internal`.

Comandos normales:

- `test.bat`
- `scripts/dev-desktop.bat`
- `scripts/reset-runtime-data.bat`
- `scripts/open-runtime-data.bat`
- `scripts/release-preflight.bat`
- `scripts/package-release-local.bat`
