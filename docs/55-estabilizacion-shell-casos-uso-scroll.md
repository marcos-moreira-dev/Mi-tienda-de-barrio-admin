# T9 — Estabilización de shell, sidebar, scrollbars y casos de uso

## Objetivo
Corregir inconsistencias visuales detectadas en la aplicación desktop de **Mi tienda de barrio admin** sin ensuciar la arquitectura ni duplicar controles hardcodeados.

## Cambios aplicados

### Shell y sidebar
- Se eliminó el segundo botón inferior del sidebar que cerraba la aplicación sin rotulación clara.
- El footer lateral conserva únicamente **Cerrar sesión**.
- En modo colapsado, el botón de cerrar sesión se representa como acceso compacto para volver al login.
- El botón de contraer/expandir sidebar se conserva visible en el bloque de marca, sin desaparecer al minimizar.
- Se ajustó el ancho colapsado para evitar clipping entre logo, botón y scroll interno.

### Scrollbars
- Se añadió una personalización global de scrollbars para que no aparezcan con apariencia nativa cruda.
- Se ocultaron flechas increment/decrement y se dejó un thumb redondeado sobrio.
- Se mantuvo el comportamiento funcional de scroll sin hardcodear estilos por pantalla.

### Casos de uso
- Se reemplazó la lista simple por una **malla catalogada** por columnas y filas:
  - Arranque
  - Inventario
  - Caja y fiado
  - Control
- Cada caso tiene código, área, módulo, objetivo, cuándo usarlo, pasos exactos y resultado esperado.
- El diseño ahora funciona como guía de capacitación y demostración, no como lista genérica.

## Archivos tocados
- `desktop/src/main/java/.../screens/shell/MainShellView.java`
- `desktop/src/main/java/.../screens/casosdeuso/CasosDeUsoView.java`
- `desktop/src/main/resources/styles/app.css`

## Validación pendiente
No se pudo ejecutar Maven en el entorno de empaquetado porque no está instalado. Validar localmente con:

```bat
scripts\dev-desktop.bat
```

