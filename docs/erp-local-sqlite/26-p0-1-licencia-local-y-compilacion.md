# P0.1 — Licencia local y recuperación de compilación

## Objetivo

Reconstruir las clases faltantes del módulo de licencia local para que los servicios de escritura, el dashboard, la salud del sistema y la vista de licencia puedan compilar contra contratos reales.

## Archivos agregados

```text
desktop/src/main/java/com/marcosmoreira/mitiendadebarrio/admin/core/application/license/WriteAccessGuard.java
desktop/src/main/java/com/marcosmoreira/mitiendadebarrio/admin/core/infrastructure/license/LicenseInfo.java
desktop/src/main/java/com/marcosmoreira/mitiendadebarrio/admin/core/infrastructure/license/LocalLicenseService.java
```

## Decisiones

- La licencia se almacena en `licencia_sistema`.
- El estado `PENDIENTE` no bloquea escritura, para no impedir el uso inicial del sistema.
- El estado `ACTIVA` permite escritura.
- El estado `VENCIDA` permite escritura mientras esté dentro del periodo de gracia.
- El estado `MODO_LIMITADO` bloquea escrituras, pero conserva consulta, reportes, exportaciones y respaldos.
- El estado `DESACTIVADA` bloquea escrituras.
- Nunca se borran datos del cliente ni se impide crear respaldos.

## Validación realizada

Se compiló con `javac --release 21` el subconjunto no JavaFX del proyecto, incluyendo bootstrap, servicios, repositorios y las nuevas clases de licencia.

Resultado: compilación correcta del subconjunto no JavaFX.

## Pendiente

La validación completa con Maven/JavaFX debe ejecutarse en el entorno del usuario porque este entorno no tiene Maven instalado ni las dependencias JavaFX descargadas.
