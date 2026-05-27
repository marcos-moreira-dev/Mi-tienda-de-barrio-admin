# Tanda 2 — Configuración del negocio y UX transversal

## Objetivo

Esta tanda convierte la arquitectura autocontenida en una base más real de aplicación:

- se mantiene la decisión JavaFX + core embebido + SQLite;
- se agrega el primer módulo vertical real: Configuración del negocio;
- se refuerzan componentes transversales de UI inspirados en Admin Patterns Lab y Marcos Moreira Admin Desktop;
- se prepara la app para operar por workspace, módulos y componentes reutilizables.

## Decisión de orden

Antes de implementar Productos, Compras o Reportes se implementa Configuración porque:

1. alimenta encabezados de reportes;
2. define identidad del negocio;
3. verifica conexión UI ↔ core ↔ SQLite;
4. prueba el flujo de formulario, validación, guardado y feedback;
5. sirve como patrón para los siguientes módulos CRUD.

## Componentes agregados

- `ActionBar`
- `AppDialog`
- `FormGrid`
- `InfoPanel`
- `ModuleScaffold`
- mejoras en `AppButton`
- actualización de CSS base

Estos componentes siguen la lógica de los proyectos referencia:

- shell fijo + workspace;
- módulos intercambiables;
- ayuda contextual;
- diálogos redimensionables para texto largo;
- formularios con etiquetas claras;
- acciones agrupadas y visibles;
- separación entre pantalla, caso de uso y repositorio.

## Core embebido agregado

- `ConfiguracionNegocio`
- `ConfiguracionNegocioRepository`
- `ConfiguracionNegocioService`
- `SqliteConfiguracionNegocioRepository`
- `LocalDatabaseMigrator`

## Flujo implementado

```text
ConfiguracionNegocioView
→ ConfiguracionNegocioService
→ ConfiguracionNegocioRepository
→ SqliteConfiguracionNegocioRepository
→ SQLite
```

## Estado

La tanda deja un primer módulo vertical conectado de punta a punta. No se declara el producto funcional completo; se declara lista la base para repetir el patrón en módulos CRUD/operativos.
