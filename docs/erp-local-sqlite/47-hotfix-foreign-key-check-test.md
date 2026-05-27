# Hotfix — Test Maven de `foreign_key_check`

## Problema

`LocalDatabaseMigratorTest` trataba `PRAGMA foreign_key_check` igual que `PRAGMA integrity_check`.

Eso es incorrecto en SQLite:

- `PRAGMA integrity_check` devuelve una fila con `ok` si todo está bien.
- `PRAGMA foreign_key_check` devuelve **cero filas** si todo está bien.

Por eso Maven fallaba aunque la base estuviera correcta.

## Corrección

Se separaron las validaciones:

- `assertIntegrityOk(...)` espera `ok`.
- `assertForeignKeyCheckOk(...)` espera cero filas y falla solo si aparece alguna violación.

## Resultado esperado

`test.bat` debe ejecutar `mvn test` en consola y los tests de Maven del desktop deben pasar.
