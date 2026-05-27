# Tandas pendientes desde V9

## Estado actual

Ya existe:

- documentación general;
- V001 base de datos 3FN;
- aplicación autocontenida JavaFX;
- core embebido mínimo;
- runtime local;
- migración V001 empaquetada;
- shell básico;
- componentes transversales;
- módulo Configuración del negocio.

## Tandas pendientes recomendadas

### Tanda 3 — Catálogos base

Módulos:

- Categorías
- Marcas
- Unidades de medida

Objetivo:
crear CRUD pequeño y reutilizable para catálogos simples.

### Tanda 4 — Proveedores

Objetivo:
crear CRUD de proveedor con búsqueda, estado activo/inactivo y observaciones.

### Tanda 5 — Productos / Inventario base

Objetivo:
primer módulo fuerte de negocio con:

- producto;
- categoría;
- marca;
- unidad;
- proveedor principal;
- precios;
- stock mínimo/objetivo;
- flags de lote/vencimiento/perecible/refrigerado;
- foto opcional.

### Tanda 6 — Movimientos de inventario

Objetivo:
formalizar entrada, salida, ajuste, merma y corrección.

### Tanda 7 — Compras / Entradas

Objetivo:
wizard o formulario operativo para recepción de mercadería.

### Tanda 8 — Salidas / Ventas internas

Objetivo:
descargo rápido de stock sin facturación tributaria.

### Tanda 9 — Reportes

Objetivo:
productos por comprar, bajo stock, agotados, vencimientos y exportación.

### Tanda 10 — Respaldos y restauración

Objetivo:
backup manual, carpeta de respaldos y restauración guiada.

### Tanda 11 — Licencia local

Objetivo:
licencia local renovable, aviso previo y modo limitado ético.

### Tanda 12 — Caja y fiado opcionales

Objetivo:
solo si se decide incluir en V1 operativa.

### Tanda 13 — Empaquetado y validación

Objetivo:
scripts, validación estructural, empaquetado con jpackage y README de instalación.

## Regla de avance

No avanzar a módulos complejos sin antes estabilizar catálogos base y productos.
