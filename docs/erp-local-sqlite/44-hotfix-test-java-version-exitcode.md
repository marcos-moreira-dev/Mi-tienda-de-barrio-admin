# Hotfix — test.bat y código de salida de java -version

Se corrigió `scripts/validate-core-no-javafx.ps1` porque la función auxiliar estaba devolviendo por el pipeline tanto la salida de `java -version` como el código de salida.

En PowerShell, eso hacía que `$javaExitCode` recibiera una mezcla de texto + `0`, provocando un falso error aunque Java estuviera instalado correctamente.

La función ahora:

- captura stdout/stderr como texto;
- imprime cada línea en consola;
- escribe cada línea en el log;
- devuelve únicamente el código entero de salida;
- mantiene `javac` como validación real de compilación.

Comando principal de prueba:

```powershell
.\test.bat
```
