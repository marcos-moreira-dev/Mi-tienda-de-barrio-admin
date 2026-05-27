# Alineación — Aplicación autocontenida

## Propósito

Este documento fija la dirección arquitectónica posterior a la documentación inicial y la V001 de base de datos.

La decisión oficial es que **Mi tienda de barrio admin** se construye como aplicación autocontenida de escritorio.

## Declaración oficial

```text
JavaFX + Core embebido + SQLite + reportes locales + respaldos locales + licencia local
```

No existe backend HTTP en V1.

## Qué significa autocontenida

El producto entregado al cliente debe poder instalarse y operar en una computadora sin depender de:

- servidor externo;
- nube;
- contenedor Docker;
- base remota;
- puerto local expuesto;
- API web;
- conexión permanente a internet.

Puede existir verificación ocasional de licencia si en el futuro se acepta internet, pero la operación diaria no depende de eso.

## Qué se mantiene de los proyectos referencia

De los proyectos anteriores se mantiene:

- seriedad documental;
- separación por capas;
- módulo de configuración;
- módulo de casos de uso;
- validaciones y errores ordenados;
- scripts de validación;
- base de datos con planificación, implementación y revisión;
- patrones JavaFX de login, loading, shell y componentes customizados.

## Qué se adapta

Lo que antes era backend se convierte en core embebido.

```text
Controller HTTP        → Controller JavaFX
Service backend        → Servicio de aplicación interno
Repository backend     → Repositorio SQLite local
DTO/API response       → OperationResult / ViewModel / Command local
Error HTTP             → AppError / BusinessException / ValidationResult
```

## Qué se elimina de V1

- API REST.
- Endpoints.
- Puerto local.
- Spring Boot.
- PostgreSQL.
- Docker Compose.
- Seguridad JWT.
- Cliente-servidor.
- Swagger/OpenAPI.

Estos conceptos pueden aparecer solo en documentación de transición futura, no en la arquitectura V1.

## Nueva fuente de verdad

1. `README.md`
2. `docs/05-arquitectura-general.md`
3. `docs/26-alineacion-aplicacion-autocontenida.md`
4. `core/docs/00-indice-core.md`
5. `desktop/docs/01-arquitectura-desktop-javafx.md`
6. `database/docs/2_implementacion_bd/70_cierre_v001_sqlite.md`

## Criterio para próximas tandas

Antes de generar código, cualquier estructura propuesta debe responder:

1. ¿Funciona sin internet?
2. ¿Funciona sin servidor?
3. ¿Funciona con SQLite local?
4. ¿Está separada la lógica de la UI?
5. ¿Está documentada la ruta de transición si deja de calzar?

Si la respuesta no es sí, no pertenece a V1.
