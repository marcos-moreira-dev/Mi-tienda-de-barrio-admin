# Roadmap de refactor y estabilización

## Ya estabilizado visualmente

- Login con identidad visual y banner del rubro.
- Sidebar y shell más cercanos al patrón de Marcos Moreira Sistema.
- Menubar superior.
- Statusbar inferior.
- Casos de uso en formato hub operativo.
- README propagandístico para GitHub y cliente.

## Refactors completados

### R3 · Componentes reutilizables UI

- Fábricas y helpers iniciales para formularios, listas, scrolls y diálogos.
- Casos de uso empezó a consumir componentes reutilizables.

### R4 · Casos de uso en capa de aplicación

- El catálogo dejó de vivir como conocimiento exclusivo de la pantalla JavaFX.
- Se movió a `core/application/casosdeuso`.
- La UI ahora presenta el catálogo en vez de inventarlo.

### R5 · Repositorios SQLite

- Todos los repositorios SQLite principales heredan de `SqliteRepositorySupport`.
- El soporte común conserva `connectionFactory`, `jdbc` y `transactions`.
- La repetición de infraestructura JDBC quedó reducida.

### R6 · Separación de formularios/listados

- Productos, compras, salidas, movimientos, caja y fiado separan formularios/listados en panes dedicados.
- Las vistas principales funcionan más como orquestadores.

### R7 · CSS por capas

- `app.css` dejó de ser la hoja principal de 1000+ líneas.
- La carga se centraliza en `ThemeStylesheets`.
- Las capas actuales son:

```text
tokens.css
base.css
controls.css
components.css
login.css
shell.css
use-cases.css
modules.css
```

- `app.css` queda solo como archivo de compatibilidad/documentación.
- `app.legacy-r56.css` conserva la sábana anterior como referencia histórica.

## Pendiente recomendado

1. Ejecutar validación local completa con Eclipse Temurin JDK 21:

```bat
scripts\validate-desktop.bat
```

2. Revisar visualmente pantalla por pantalla:

```bat
scripts\dev-desktop.bat
```

3. Corregir detalles finos de UX si aparecen al usar la app real:

- ancho de columnas;
- textos cortados;
- scrolls internos;
- botones con jerarquía visual confusa;
- formularios que requieran microcopy más claro.

4. Congelar UI.

5. Planificar empaquetado/instalador para cliente final.

## Regla para futuras tandas

No volver a meter estilos nuevos en `app.css`.

- Estilo de controles JavaFX: `controls.css`.
- Componentes reutilizables: `components.css`.
- Sidebar, topbar, menubar, workspace y statusbar: `shell.css`.
- Login y loading: `login.css`.
- Casos de uso: `use-cases.css`.
- Ajustes de pantallas concretas del negocio: `modules.css`.
