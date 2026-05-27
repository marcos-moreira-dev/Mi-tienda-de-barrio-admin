# 64 · R7 · CSS por capas y cierre de refactor visual

## Objetivo

Cerrar la tanda R7 separando el antiguo `app.css` monolítico en capas mantenibles, comentadas y cargadas de forma explícita desde Java.

El objetivo no fue rediseñar la aplicación, sino reducir malos olores:

- evitar una única hoja de 1000+ líneas;
- separar tokens, shell, controles, componentes y módulos;
- dejar una ruta clara para futuras tandas sin seguir ensuciando `app.css`;
- mantener el patrón visual ya estabilizado de login, sidebar, casos de uso y componentes base.

## Decisión principal

Desde esta tanda la escena JavaFX carga estilos mediante:

```text
desktop/src/main/java/com/marcosmoreira/mitiendadebarrio/admin/desktop/ui/theme/ThemeStylesheets.java
```

Esta clase centraliza el orden de carga:

```text
1. tokens.css
2. base.css
3. controls.css
4. components.css
5. login.css
6. shell.css
7. use-cases.css
8. modules.css
```

El archivo `app.css` queda como archivo de compatibilidad/documentación, no como lugar para seguir agregando reglas.

## Archivos creados

```text
desktop/src/main/resources/styles/tokens.css
desktop/src/main/resources/styles/base.css
desktop/src/main/resources/styles/controls.css
desktop/src/main/resources/styles/components.css
desktop/src/main/resources/styles/login.css
desktop/src/main/resources/styles/shell.css
desktop/src/main/resources/styles/use-cases.css
desktop/src/main/resources/styles/modules.css
desktop/src/main/resources/styles/app.legacy-r56.css
```

También se agregó:

```text
desktop/src/main/java/com/marcosmoreira/mitiendadebarrio/admin/desktop/ui/theme/ThemeStylesheets.java
```

## Responsabilidad de cada capa

### `tokens.css`

Define la identidad visual compartida:

- colores del producto;
- superficies;
- bordes;
- sombras;
- tipografía base;
- textos globales.

Regla: si cambia la marca o paleta, primero se revisa esta capa.

### `base.css`

Contiene reglas mínimas de escena:

- pantalla centrada;
- fondo base;
- normalización mínima de `ScrollPane`.

Regla: no debe crecer con estilos de módulos concretos.

### `controls.css`

Centraliza controles nativos JavaFX:

- `TextField`;
- `PasswordField`;
- `TextArea`;
- `ComboBox`;
- `DatePicker`;
- `CheckBox`;
- `TableView`;
- `TabPane`;
- `ScrollBar`.

Regla: cualquier corrección de controles nativos va aquí, no en cada pantalla.

### `components.css`

Estilos de componentes propios reutilizables:

- `AppCard`;
- `AppButton`;
- `StatusBadge`;
- `InfoPanel`;
- `ModuleScaffold`;
- `MetricCard`;
- helpers R3 (`app-scroll`, `app-list-bullet`, `app-check-box`).

Regla: si un componente se usa en varias pantallas, su estilo vive aquí.

### `login.css`

Contiene login y loading:

- panel azul;
- banner de supermercado como fondo;
- logo;
- formulario;
- credenciales iniciales;
- barra de carga.

Regla: no mezclar login con sidebar ni módulos.

### `shell.css`

Contiene el marco principal:

- `MenuBar`;
- `TopBar`;
- `Sidebar`;
- modo colapsado;
- botón de cerrar sesión;
- workspace;
- statusbar.

Regla: todo lo relacionado al layout general va aquí.

### `use-cases.css`

Contiene el hub de casos de uso:

- intro;
- panel de módulos;
- panel de casos;
- detalle;
- pasos.

Regla: la sección de casos de uso no debe depender de `modules.css` ni de estilos hardcodeados.

### `modules.css`

Reservado para afinamientos de pantallas de negocio:

- productos;
- compras;
- salidas;
- movimientos;
- caja;
- fiado;
- reportes;
- respaldos.

Por ahora queda liviano para evitar meter deuda nueva.

## Comentarios y mantenibilidad

Cada archivo nuevo incluye comentario de propósito al inicio. La intención es que una IA, Codex o el propio desarrollador no vuelva a convertir `app.css` en una sábana.

## Criterio de aceptación

Esta tanda se considera cerrada si:

- la aplicación carga estilos desde `ThemeStylesheets`;
- los archivos CSS existen y están en orden claro;
- `app.css` ya no es el contenedor principal de reglas;
- el estilo visual actual se conserva;
- futuras reglas tienen una ubicación evidente.

## Validación local recomendada

En Windows, con Eclipse Temurin JDK 21 y Maven Toolchain ya configurado:

```bat
scripts\validate-desktop.bat
```

Para probar visualmente:

```bat
scripts\dev-desktop.bat
```

## Nota

No se cambió lógica de negocio, repositorios, esquema SQLite ni casos de uso. Esta tanda es de infraestructura visual y arquitectura de estilos.
