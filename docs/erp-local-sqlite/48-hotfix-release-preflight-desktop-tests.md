# Hotfix — preflight de release y tests Maven desktop

## Problema corregido

El preflight estaba invocando `Assert-Contains` con varios archivos en una sola llamada. PowerShell concatenaba esas rutas y luego intentaba validar una ruta inexistente formada por varios nombres de archivo pegados.

Eso producía errores falsos como:

```text
No se puede revisar archivo ausente: desktop\src\main\java\...LocalDatabaseMigrator.java desktop\src\test\java\...AppBootstrapSmokeTest.java ...
```

## Corrección

Se separó la revisión del migrador y de los tests Maven del desktop:

- `LocalDatabaseMigrator.java` se revisa de forma individual para confirmar V001 consolidada, `PRAGMA integrity_check` y `PRAGMA foreign_key_check`.
- Los tests Maven del desktop se revisan mediante una tabla de archivos y fragmentos esperados.

## Resultado esperado

`test.bat` debe ejecutar:

1. validación core sin JavaFX;
2. `mvn test` del desktop;
3. validaciones SQL/locales;
4. preflight de release sin falsos negativos por rutas concatenadas.
