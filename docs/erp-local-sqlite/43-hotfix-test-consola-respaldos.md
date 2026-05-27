# Hotfix — test.bat con salida útil en consola y validación de respaldos

## Motivo

Durante la validación en Windows se detectaron dos puntos:

1. `validate-core-no-javafx.ps1` guardaba la salida de `javac` en log, pero no la mostraba en consola, dificultando copiar y pegar el error rápidamente.
2. `validate-respaldos-seguros.ps1` buscaba el texto literal `SQLite format 3` dentro de `RespaldoService`, aunque la validación real usa la constante binaria `SQLITE_HEADER`.

## Corrección

- La salida de `java` y `javac` ahora se escribe en el log y también se muestra en consola.
- La validación de respaldos ahora busca `SQLITE_HEADER`, que es la implementación real de cabecera SQLite en el servicio.

## Prueba recomendada

Desde la raíz del proyecto:

```powershell
.\test.bat
```

Si falla `javac`, el error debe verse tanto en consola como en `.diagnostics/logs`.
