# Cierre de alineación — Core embebido

## Estado

La documentación queda alineada para tratar el sistema como una aplicación autocontenida.

## Decisiones cerradas

- V1 no tendrá backend HTTP.
- V1 no tendrá Spring Boot.
- V1 no tendrá PostgreSQL.
- V1 no tendrá API REST.
- V1 no tendrá Docker.
- V1 sí tendrá JavaFX.
- V1 sí tendrá SQLite local.
- V1 sí tendrá core embebido con capas internas.
- V1 sí tendrá reportes, respaldos y licencia local.

## Siguiente fase sugerida

La siguiente fase no debe llamarse backend. Debe llamarse:

```text
Tanda Core Embebido 1 — estructura base de aplicación autocontenida
```

Objetivo de esa fase:

- definir paquetes Java;
- crear proyecto Maven/Gradle JavaFX;
- configurar SQLite driver;
- crear bootstrap de aplicación;
- crear inicializador de runtime local;
- preparar migraciones V001;
- crear servicios base de configuración, licencia, logs y base de datos;
- no implementar todavía todos los módulos de negocio.

## Advertencia

Aunque el proyecto use disciplina de backend, no debe volver a arrastrar nombres como:

- endpoint;
- controller REST;
- ApiResponse;
- JWT;
- Swagger;
- Spring profiles;
- localhost server.

Equivalentes locales:

- caso de uso;
- controlador JavaFX;
- OperationResult;
- sesión local simple;
- manual de ayuda;
- configuración local;
- inicializador de aplicación.

## Condición para avanzar a código

Antes de generar código fuente, revisar:

- `core/docs/02-arquitectura-core-embebido.md`
- `core/docs/03-servicios-aplicacion-y-casos-de-uso.md`
- `core/docs/06-repositorios-sqlite.md`
- `core/docs/07-transacciones-sqlite.md`
- `desktop/docs/18-integracion-javafx-core-embebido.md`
- `docs/21-nomenclatura-y-convenciones.md`
