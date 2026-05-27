# Tanda 14 — Modo limitado de licencia aplicado a escrituras

## Objetivo

Aplicar la regla ética de licenciamiento: si la licencia vence y termina el periodo de gracia, el sistema no secuestra datos, pero limita operaciones de escritura.

## Implementado

- `WriteAccessGuard` como componente transversal.
- Exposición del guard en `AppContext`.
- Integración inicial en `MainShellView`.
- Bloqueo de navegación hacia módulos principalmente transaccionales cuando la licencia está en modo limitado.

## Módulos de consulta permitidos en modo limitado

- Inicio.
- Reportes.
- Respaldos.
- Licencia.
- Ayuda.

## Módulos bloqueados en modo limitado

- Configuración.
- Catálogos.
- Proveedores.
- Productos.
- Movimientos.
- Compras.
- Salidas.
- Caja.
- Fiado.

## Decisión de diseño

Esta tanda aplica el control a nivel de carcasa/navegación. Una tanda posterior puede reforzar el control también dentro de cada servicio de aplicación para defensa en profundidad.

## Regla ética

El sistema debe permitir consulta, reportes, exportación y respaldo aunque la licencia esté vencida. No debe borrar datos ni impedir que el cliente se lleve su información.

