# Plan UX/UI inteligente para módulos JavaFX

## Principio

La aplicación debe sentirse como herramienta de trabajo para una tienda real, no como experimento técnico.

## Patrones tomados de referencia

De Admin Patterns Lab:

- shell + workspace;
- catálogo/CRUD;
- bandeja operativa;
- wizard;
- reportes;
- configuración;
- ayuda contextual;
- panel informativo por módulo.

De Marcos Moreira Admin Desktop:

- login/loading;
- shell principal;
- componentes transversales;
- mensajes largos en diálogos redimensionables;
- documentación por módulo;
- tooltips y ayuda contextual;
- responsabilidad única en vistas y componentes.

## Familias de pantalla

### 1. Pantalla de configuración

Uso:
- datos del negocio;
- licencia;
- parámetros locales.

Patrón:
- formulario principal;
- panel lateral de ayuda;
- barra de acciones.

### 2. Catálogo / CRUD

Uso:
- productos;
- categorías;
- marcas;
- unidades;
- proveedores.

Patrón:
- filtro superior;
- tabla central;
- formulario lateral o diálogo;
- acciones claras.

### 3. Bandeja operativa

Uso:
- compras pendientes;
- productos bajos;
- productos vencidos;
- cuentas por cobrar.

Patrón:
- lista priorizada;
- filtros rápidos;
- estado visible;
- acción principal por fila.

### 4. Wizard

Uso:
- registrar compra;
- registrar venta interna;
- restaurar respaldo.

Patrón:
- pasos cortos;
- validación por paso;
- resumen final antes de confirmar.

### 5. Reportes

Uso:
- productos por comprar;
- bajo stock;
- vencimientos;
- caja;
- fiado.

Patrón:
- filtros arriba;
- vista previa;
- exportar PDF/CSV;
- ayuda lateral.

## Componentes transversales obligatorios

- `ModuleScaffold`
- `ActionBar`
- `InfoPanel`
- `FormGrid`
- `AppDialog`
- `AppCard`
- `EmptyState`
- `StatusBadge`
- `AppButton`

## Regla

Cada módulo nuevo debe declarar primero qué familia de pantalla usa. No se diseña módulo por módulo desde cero.
