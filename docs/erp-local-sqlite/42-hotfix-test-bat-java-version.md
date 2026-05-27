# Hotfix — test.bat y java -version en Windows PowerShell

## Problema

Al ejecutar `scripts/test.bat`, la primera validación podía detenerse en Windows PowerShell porque `java -version` escribe su salida por `stderr` aunque termine correctamente.

Con `$ErrorActionPreference = 'Stop'`, PowerShell puede interpretar esa salida como `NativeCommandError`.

## Corrección

Se ajustó `scripts/validate-core-no-javafx.ps1` para ejecutar comandos nativos mediante una función auxiliar que:

- captura `stdout` y `stderr` como texto;
- guarda la salida en el log;
- revisa el código de salida real;
- no trata la salida normal de `java -version` como error.

## Validación esperada

Desde la raíz del proyecto:

```powershell
.\test.bat
```

O desde `scripts`:

```powershell
.\test.bat
```

La validación debe avanzar más allá de `java -version` y continuar con la compilación del core.
