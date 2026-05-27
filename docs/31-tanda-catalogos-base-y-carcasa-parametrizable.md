# Tanda 1 — Catálogos base y carcasa parametrizable

## Objetivo

Implementar el primer módulo transversal de negocio después de Configuración: **Catálogos base**.

La tanda también corrige una decisión de arquitectura de interfaz: la carcasa principal debe ser parametrizable para poder reutilizarse en futuras aplicaciones locales sin quedar amarrada a una tienda específica.

## Alcance implementado

### Core embebido

Se agregaron servicios y puertos para:

- Categorías.
- Marcas.
- Unidades de medida.

Cada catálogo tiene:

- entidad de dominio;
- puerto de repositorio;
- servicio de aplicación;
- adaptador SQLite;
- validaciones mínimas;
- activación/desactivación lógica.

### Desktop JavaFX

Se agregó el módulo:

- `CatalogosBaseView`.

Incluye pestañas para:

- Categorías.
- Marcas.
- Unidades.

Cada pestaña permite:

- listar registros;
- mostrar inactivos;
- crear;
- editar;
- activar/desactivar.

### Carcasa parametrizable

Se agregaron:

- `AppShellDescriptor`.
- `AppModuleDescriptor`.

La carcasa ahora recibe:

- nombre del producto;
- subtítulo;
- mensaje de inicio;
- lista de módulos visibles;
- estado de módulos disponibles o pendientes.

Esto permite reutilizar la misma shell en otra app local cambiando el descriptor, no el código de navegación.

## Principio de responsabilidad única aplicado

- `AppShellDescriptor`: describe la carcasa.
- `AppModuleDescriptor`: describe un acceso de navegación.
- `MainShellView`: renderiza la carcasa y enruta hacia vistas.
- `CatalogosBaseView`: agrupa catálogos.
- Cada panel de catálogo maneja solo su formulario y lista.
- Cada servicio de catálogo maneja validaciones y casos de uso de su entidad.
- Cada repositorio SQLite maneja persistencia de una sola tabla.

## Decisión UX/UI

Los catálogos se implementan como módulo transversal, no como módulo comercial complejo.

Reglas:

- Debe ser fácil crear una categoría antes de crear productos.
- No se elimina físicamente; se desactiva.
- Los nombres deben ser claros y no técnicos.
- Las unidades deben permitir decimales solo cuando el negocio lo necesita.

## Validación pendiente

No se pudo compilar con Maven en este entorno porque `mvn` no está instalado.

Validación local recomendada:

```powershell
cd desktop
mvn javafx:run
```

Luego probar:

1. Abrir la app.
2. Entrar a Catálogos.
3. Crear categoría.
4. Crear marca.
5. Crear unidad.
6. Editar cada registro.
7. Desactivar/reactivar.
8. Cerrar y reabrir para verificar persistencia SQLite.

## Siguiente tanda sugerida

**Tanda 2 — Proveedores.**

Razón: Proveedores depende de catálogos solo conceptualmente y prepara el módulo de Productos/Compras.
