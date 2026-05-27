# Tanda 12 — Ayuda contextual / mini manual interno

## Objetivo
Agregar ayuda contextual integrada para reducir dependencia del desarrollador y facilitar uso por parte de dueños con baja o media cultura tecnológica.

## Alcance implementado
- Dominio: `AyudaContextual`.
- Core embebido: `AyudaContextualService` y repositorio.
- Infraestructura: `SqliteAyudaContextualRepository`.
- UI JavaFX: `AyudaContextualView`.
- Seed automático de ayuda base si la tabla está vacía.

## Decisiones
- La ayuda vive dentro de la app.
- La ayuda no reemplaza capacitación inicial, pero sirve como manual corto.
- La información se organiza por módulo.
