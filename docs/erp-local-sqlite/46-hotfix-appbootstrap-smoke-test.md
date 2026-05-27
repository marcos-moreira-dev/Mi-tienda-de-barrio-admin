# Hotfix — AppBootstrapSmokeTest alineado con la API real

## Problema

El `test.bat` ya ejecutaba `mvn test` para el desktop, pero el test `AppBootstrapSmokeTest` asumía una API incorrecta:

- intentaba construir `new AppBootstrap()` aunque `AppBootstrap` tiene constructor privado;
- esperaba un `StartupReport`, aunque `AppBootstrap.start()` devuelve directamente `AppContext`;
- llamaba `report.context()`, método que no existe en `StartupReport`.

## Corrección

`AppBootstrapSmokeTest` ahora usa la API real:

```java
AppContext context = AppBootstrap.start();
RuntimePaths paths = context.paths();
```

Y verifica que el bootstrap cree el contexto sin lanzar JavaFX, usando un runtime temporal mediante:

```java
System.setProperty("mitienda.runtime.root", tempDir.toString());
```

## Resultado esperado

`mvn test` debe compilar y ejecutar los tests del desktop. La salida de Maven se imprime directamente en consola para poder copiar y pegar el error completo sin revisar logs.
