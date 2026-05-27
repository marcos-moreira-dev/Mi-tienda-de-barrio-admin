# Logs y salidas esperadas

## Propósito

Estandarizar cómo deben verse los mensajes de scripts y logs.

## Convención

Cada script debe mostrar:

- nombre del proyecto;
- ruta de trabajo;
- acción principal;
- ruta del log;
- resultado final.

## Ejemplo

```text
== Mi tienda de barrio admin :: validación documental ==
Root: C:\...
Log: .mi-tienda-admin\logs\validate-docs_YYYYMMDD_HHMMSS.log
Resultado: OK
```

## Regla

El usuario no debe adivinar si algo falló. El script debe decirlo claramente.
