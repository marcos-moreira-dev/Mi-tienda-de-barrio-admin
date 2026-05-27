# Arquitectura general

## Decisión oficial

**Mi tienda de barrio admin es una aplicación autocontenida de escritorio.**

La V1 usa:

```text
JavaFX + Core embebido + SQLite + reportes locales + respaldos locales + licencia local
```

No usa backend HTTP, API REST, Spring Boot local, PostgreSQL, Docker, servidor en `localhost` ni arquitectura cliente-servidor.

## Razón

El cliente objetivo es una tienda/despensa de una sola computadora, con baja o media cultura tecnológica y necesidad de abrir/cerrar una aplicación local. SQLite encaja mejor cuando todo vive en el mismo equipo y la operación no requiere concurrencia multi-PC.

## Flujo interno

```text
Pantalla JavaFX
→ Controlador JavaFX
→ Caso de uso / servicio de aplicación
→ Repositorio SQLite
→ Base de datos local
```

Ejemplo:

```text
ProductosViewController
→ RegistrarProductoUseCase
→ ProductoService
→ ProductoRepositorySqlite
→ mi_tienda_barrio.sqlite
```

## Capas

```text
ui/             # JavaFX, vistas, controllers, dialogs y componentes customizados
application/    # casos de uso, servicios de aplicación y orquestación transaccional
domain/         # entidades, reglas, enums, value objects e invariantes
infrastructure/ # SQLite, reportes, backups, licencia, logs, configuración, archivos
shared/         # OperationResult, errores, paginación local, utilidades transversales justificadas
```

## Core embebido

El “core embebido” no es un servidor. Es la lógica interna separada de JavaFX y de SQLite aunque viva dentro del mismo ejecutable.

Agrupa conceptualmente:

- casos de uso;
- servicios de aplicación;
- reglas de dominio;
- validaciones;
- resultados locales;
- errores de negocio;
- puertos/repositorios;
- transacciones sobre SQLite.

## Adaptación de patrones anteriores

De Marcos Moreira Admin:

- documentación por capas;
- módulo de configuración;
- módulo de casos de uso;
- scripts claros;
- trazabilidad humana;
- cierre formal de base de datos;
- separación entre planificación, implementación y revisión.

De Admin Patterns Lab:

- CRUD;
- wizard;
- bandeja;
- expediente;
- login;
- loading;
- shell;
- componentes customizados;
- ayuda contextual;
- arquitectura de interacción.

## Diferencia frente a proyectos anteriores

En este proyecto no se replica la arquitectura backend/desktop. Se conserva la disciplina, pero se adapta a una app autocontenida.

```text
Antes: Desktop → Backend HTTP → PostgreSQL
Ahora: Desktop JavaFX → Core embebido → SQLite local
```

## Runtime local

```text
runtime/
├── data/
├── backups/
├── reports/
├── images/products/
├── logs/
├── config/
└── license/
```

## Principios

1. Controladores JavaFX delgados.
2. Casos de uso explícitos.
3. Reglas fuera de la UI.
4. SQLite encapsulado tras repositorios.
5. Transacciones en operaciones críticas.
6. Reportes, respaldos y licencia como servicios internos.
7. Cero dependencia de internet para operar normalmente.
8. Ruta clara de transición si el negocio exige varias computadoras o facturación.
