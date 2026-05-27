# Índice general — Mi tienda de barrio admin

## Documentos generales

- `01-vision-del-producto.md`: visión, usuario, problema y promesa.
- `02-investigacion-del-rubro.md`: síntesis de tiendas/despensas en Guayaquil/Ecuador.
- `03-alcance-vendible.md`: qué entra, qué queda opcional y qué queda fuera.
- `04-modelo-comercial-y-licenciamiento.md`: paquetes, licencia, soporte y modo limitado.
- `05-arquitectura-general.md`: JavaFX + SQLite y estructura modular.
- `06-modelo-de-datos-general.md`: entidades y relaciones principales.
- `07-casos-de-uso-generales.md`: mapa global de operaciones.
- `08-reglas-de-negocio.md`: reglas de stock, compras, salidas, reportes y licencia.
- `09-validaciones-y-errores.md`: validaciones y mensajes entendibles.
- `10-experiencia-de-usuario.md`: criterios UX/UI para baja o media cultura tecnológica.
- `11-seguridad-local-y-respaldos.md`: datos locales, backups, apagones y límites.
- `12-transicion-a-sistema-avanzado.md`: cuándo migrar a PostgreSQL + backend centralizado centralizado.
- `13-riesgos-y-limitaciones.md`: riesgos comerciales, técnicos y operativos.
- `14-guia-de-levantamiento.md`: preguntas para visitas a tiendas.
- `15-checklist-visita-negocio.md`: observación de campo.
- `16-glosario-del-dominio.md`: vocabulario del sistema.
- `17-roadmap-del-producto.md`: fases de evolución.
- `18-matriz-consistencia-v1.md`: cruce de módulos, entidades, alcance y decisiones V1.
- `19-cierre-documental-previo-base-datos.md`: congelamiento documental antes de diseñar SQLite.
- `20-brief-para-v001-base-datos.md`: puente hacia la primera migración.
- `21-nomenclatura-y-convenciones.md`: nombres de producto, carpetas, clases, tablas y documentos.
- `22-revision-antiduplicados.md`: fuentes de verdad y control de duplicados.

## Regla de verdad

Cuando haya conflicto entre documentos, priorizar:

1. README raíz.
2. Este índice.
3. Visión del producto.
4. Alcance vendible.
5. Documento específico del módulo.
6. Documentación técnica de database, desktop o scripts.

## Estado de esta fase

Esta fase es de diseño estructural y documentación. No generar código fuente detallado hasta cerrar alcance, datos, casos de uso, UX, licencia y estructura local.

## Alineación autocontenida posterior

- `26-alineacion-aplicacion-autocontenida.md`: fija que V1 es JavaFX + core embebido + SQLite, sin backend HTTP.
- `27-cierre-alineacion-core-embebido.md`: cierre de decisiones para pasar a implementación autocontenida.
- `core/docs/00-indice-core.md`: índice del núcleo embebido interno.

## Nota de nomenclatura

Cuando los documentos antiguos mencionen “backend” como patrón conceptual, debe leerse como **core embebido** salvo que el contexto sea transición futura a sistema avanzado.

## Tandas de implementación recientes

- `docs/31-tanda-catalogos-base-y-carcasa-parametrizable.md`
- `docs/32-tandas-pendientes-desde-v10.md`


## Tandas recientes de implementación

- [36 — Movimientos de inventario](36-tanda-movimientos-inventario.md)
- [37 — Compras / Entradas](37-tanda-compras-entradas.md)
- [38 — Salidas / Ventas internas](38-tanda-salidas-ventas-internas.md)
- [39 — Tandas pendientes desde V12](39-tandas-pendientes-desde-v12.md)

- [59 · R3 componentes reutilizables UI](59-r3-componentes-reutilizables-ui.md)
- [60 · R4 casos de uso en capa de aplicación](60-r4-casos-de-uso-capa-aplicacion.md)
