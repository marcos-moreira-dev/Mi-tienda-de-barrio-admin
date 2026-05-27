# Tanda Core Embebido 1 — Estructura fuente

## Propósito

Definir la primera estructura fuente del core embebido de la aplicación autocontenida.

## Regla principal

El core no es backend HTTP. Es la capa interna que contiene casos de uso, reglas, servicios, puertos, repositorios e infraestructura local.

## Paquetes creados

```text
com.marcosmoreira.mitiendadebarrio.admin
├── bootstrap
├── core
│   ├── application
│   ├── domain
│   └── infrastructure
├── desktop
│   └── ui
└── shared
    ├── exception
    └── result
```

## Sustituciones oficiales

| Patrón web/backend | Sustitución autocontenida |
|---|---|
| Controller REST | JavaFX Controller / View event handler |
| ApiResponse | OperationResult |
| PageResponse HTTP | PageResult local |
| GlobalExceptionHandler | AppDialog / ErrorPresenter |
| Repository JPA | Repository SQLite/JDBC |
| SecurityConfig/JWT | Login local/licencia/perfil simple |
| RequestLoggingFilter | AppLogger local |
| Endpoint | Caso de uso interno |

## Próxima tanda

Implementar un primer módulo vertical mínimo, preferiblemente **Configuración del negocio**, porque permite validar UI, core, SQLite, validaciones y mensajes sin inflar el dominio.
