# Tanda UI - Shell y login inspirados en Marcos Moreira Sistema

## Motivo

La aplicación ya compilaba y abría, pero la presentación visual todavía era demasiado simple para enseñar a un cliente real. Esta tanda no cambia reglas de negocio: mejora el marco visual, la navegación y la percepción de producto terminado.

## Cambios principales

- Shell principal con barra lateral oscura, grupos de módulos e iconos de navegación.
- Topbar con título del módulo activo, subtítulo contextual y badges de modo local, SQLite y licencia.
- Barra inferior con estado operativo.
- Login local con estructura de dos columnas: panel comercial a la izquierda y formulario a la derecha.
- CSS centralizado con tokens visuales inspirados en el proyecto Marcos Moreira Sistema, sin reutilizar logo MMS.
- Reportes genera el reporte inicial al abrir para evitar una pantalla excesivamente vacía.

## Criterio de producto

Se mantiene el enfoque de cliente real:

- No se llama demo a clases, rutas ni conceptos internos.
- La presentación comercial sigue separada por scripts.
- La base limpia de cliente no recibe datos inventados automáticamente.

## Archivos tocados

- `desktop/src/main/java/.../desktop/ui/screens/shell/MainShellView.java`
- `desktop/src/main/java/.../desktop/ui/screens/login/LoginView.java`
- `desktop/src/main/java/.../desktop/ui/screens/loading/LoadingView.java`
- `desktop/src/main/java/.../desktop/ui/screens/reportes/ReportesOperativosView.java`
- `desktop/src/main/resources/styles/app.css`
- `desktop/src/main/resources/assets/images/nav/`

## Verificación pendiente en Windows

Ejecutar:

```bat
scripts\dev-desktop.bat
```

Validar visualmente:

1. Login en dos columnas.
2. Ingreso con `admin / admin123456`.
3. Sidebar oscuro con módulos agrupados.
4. Topbar cambia al navegar.
5. Reportes ya no abre como pantalla totalmente vacía.
