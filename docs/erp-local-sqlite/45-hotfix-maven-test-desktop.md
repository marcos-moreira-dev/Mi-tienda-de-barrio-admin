# Hotfix — test.bat ejecuta Maven test del desktop

## Decisión

El `test.bat` debe ejecutar también la fase de tests de Maven del proyecto desktop, no solo `mvn -DskipTests compile`.

## Cambio aplicado

Se actualizó:

- `scripts/internal/validate-desktop.ps1`
- `scripts/internal/release-preflight.ps1`
- `scripts/README.md`

Ahora la validación desktop ejecuta:

```powershell
mvn test
```

Esto activa la fase estándar de pruebas de Maven para el módulo desktop.

## Nota

Actualmente el proyecto puede tener pocos o ningún test unitario JUnit formal. Aun así, `mvn test` valida el ciclo Maven completo hasta la fase de test, compila el desktop y deja listo el punto para que futuros tests de Maven se ejecuten automáticamente desde `test.bat`.

## Preflight

El preflight ahora reconoce `scripts/internal/validate-desktop.bat` como parte obligatoria de la cadena de pruebas. También evita llenar la consola con advertencias por archivos generados dentro de `desktop/target`, porque esa carpeta aparece normalmente después de ejecutar Maven.
