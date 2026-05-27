# Core embebido — Catálogos base

## Propósito

Documentar la implementación del vertical slice de catálogos base: categorías, marcas y unidades de medida.

## Capas involucradas

```text
UI JavaFX
→ Servicio de aplicación
→ Puerto de repositorio
→ Adaptador SQLite
→ Tablas categoria / marca / unidad_medida
```

## Decisiones

- Categorías, marcas y unidades son catálogos transversales.
- No se eliminan físicamente; se desactivan.
- La UI permite mostrar u ocultar inactivos.
- La validación principal ocurre en el servicio de aplicación.
- La unicidad final la protege SQLite.

## Responsabilidad única

- Servicio: valida y coordina caso de uso.
- Repositorio: persiste y consulta.
- Vista: captura datos y muestra feedback.
- Descriptor de carcasa: no conoce reglas de negocio.

## Riesgos

- Catálogos demasiado complejos antes de Productos.
- Nombres duplicados por escritura inconsistente.
- Unidades mal configuradas para productos por peso.

## Mitigación

- Usar `COLLATE NOCASE` en nombres.
- Desactivar en vez de borrar.
- Mantener formulario corto.
