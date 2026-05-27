# R4 — Casos de uso como catálogo de aplicación

## Objetivo

Sacar el catálogo funcional de casos de uso fuera de la pantalla JavaFX. Los casos de uso describen capacidades del negocio, no botones ni layout.

## Implementado

Se movió el catálogo desde:

```text
desktop/ui/screens/casosdeuso/UseCaseCatalog.java
```

hacia:

```text
core/application/casosdeuso/UseCaseCatalog.java
```

Se agregó:

```text
core/application/casosdeuso/UseCaseCatalogService.java
```

## Responsabilidad actual

`UseCaseCatalog` mantiene la matriz documentada de módulos y casos.

`UseCaseCatalogService` expone operaciones de aplicación de solo lectura:

- listar módulos;
- contar casos totales;
- obtener primer módulo;
- obtener primer caso de un módulo;
- buscar caso por código.

`CasosDeUsoView` queda reducida a presentar módulos, chips de casos y detalle operativo. Ya no es la dueña conceptual del catálogo.

## Beneficio

La app puede reutilizar el catálogo en el futuro para:

- ayuda contextual por módulo;
- búsqueda de casos de uso;
- mini manual embebido;
- generación de reportes de capacitación;
- validación de cobertura funcional antes de entregar al cliente.

## Regla para próximas tandas

La UI no debe crear reglas de negocio ni catálogos funcionales largos dentro de la pantalla. Debe consumir servicios de aplicación o modelos preparados por el core.
