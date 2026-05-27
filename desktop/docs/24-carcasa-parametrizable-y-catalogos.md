# Desktop — Carcasa parametrizable y catálogos base

## Objetivo

Alinear la interfaz con dos principios:

1. La carcasa debe poder reutilizarse para otra aplicación local.
2. Los catálogos base deben ser fáciles de operar antes del módulo Productos.

## Carcasa parametrizable

La shell usa:

- `AppShellDescriptor`.
- `AppModuleDescriptor`.

Esto evita quemar nombres de módulos directamente dentro del layout principal.

## Límites

La parametrización aplica principalmente a:

- nombre visible de la app;
- subtítulo;
- home;
- lista de módulos;
- módulos disponibles o pendientes.

No aplica todavía a:

- formulario interno de cada módulo;
- reglas de negocio;
- modelo de datos.

## Catálogos base

Se implementan como pestañas:

- Categorías.
- Marcas.
- Unidades.

El patrón visual usado es:

```text
Lista izquierda + formulario derecho + acciones inferiores
```

Este patrón se puede reutilizar para otros catálogos simples.

## Recomendación para futuras apps

Para una nueva app local, crear otro método de fábrica en `AppShellDescriptor` o leerlo desde configuración local en una tanda posterior.
