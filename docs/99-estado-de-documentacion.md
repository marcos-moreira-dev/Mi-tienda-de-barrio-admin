# Estado de documentación

## Estado actual

Se completó una fase amplia de documentación para **Mi tienda de barrio admin**.

La documentación ya cubre visión, investigación del rubro, alcance vendible, módulos, reglas de negocio, validaciones, experiencia de usuario, seguridad local, respaldos, transición avanzada, comercial, operación, desktop, database, scripts y decisiones arquitectónicas.

## Tandas completadas

### Tanda 1

Documentación general inicial, README raíz, arquitectura, rubro, alcance, modelo comercial y estructura de carpetas.

### Tanda 2

Documentación específica por módulos en `docs/modulos/`, siguiendo patrón de propósito, decisiones, casos de uso, flujos, manual, componentes, mensajes y pruebas.

### Tanda 3

Refuerzo técnico en `database/docs/`, `desktop/docs/`, `scripts/docs/`, `docs/distribucion/`, `docs/operacion/`, `docs/comercial/` y ADRs.

### Tanda 4

Consistencia general, índices, lectura mínima y alineación con el nombre **Mi tienda de barrio admin**.

### Tanda 5

Cierre de consistencia V1, matriz de módulos/entidades/alcance, cierre previo a base de datos y brief para `V001`.

## Documentos clave añadidos en cierre

- `docs/18-matriz-consistencia-v1.md`
- `docs/19-cierre-documental-previo-base-datos.md`
- `docs/20-brief-para-v001-base-datos.md`
- `docs/21-nomenclatura-y-convenciones.md`
- `docs/22-revision-antiduplicados.md`

## Decisión

La documentación está suficientemente cerrada para pasar a la fase de base de datos cuando el usuario lo indique.

## Siguiente paso recomendado

No pasar aún a JavaFX. El siguiente paso lógico es:

1. revisar `docs/20-brief-para-v001-base-datos.md`;
2. decidir si caja/fiado/lote entran físicamente desde V001;
3. diseñar tablas físicas;
4. preparar `V001__schema_inicial.sql`;
5. preparar seeds mínimos.

## Pendientes no bloqueantes

- Manual final de usuario, cuando exista UI real.
- Contrato/acuerdo simple, cuando se cierre modelo de venta.
- Scripts reales, cuando exista código fuente.
- Wireframes finales, cuando se pase a desktop.

## Actualización — Tanda V001 base de datos

Se inició formalmente la base de datos siguiendo el procedimiento del proyecto de referencia.

Se crearon documentos de planificación e implementación en:

```text
database/docs/1_planificacion_bd/
database/docs/2_implementacion_bd/
```

Se creó la migración oficial:

```text
database/sql/migrations/V001__schema_3fn_oficial.sql
```

También se crearon:

```text
database/sql/archivo/V001__schema_bruto.sql
database/sql/seeds/V001__seed_presentacion.sql
database/sql/seeds/V001__reset_presentacion.sql
```

La documentación ya permite pasar a validación técnica de SQLite antes de implementar JavaFX.
## Tanda revisión exhaustiva base de datos V001

Estado: completada.

Se agregó y revisó formalmente:

- SQL 1FN legacy.
- SQL bruto formal.
- Informe específico de 1FN.
- Informe de normalización ampliado.
- Matriz de dependencias funcionales.
- Matriz conceptual/lógica/física.
- Revisión exhaustiva V001.
- Smoke check auxiliar.

La fuente oficial sigue siendo `database/sql/migrations/V001__schema_3fn_oficial.sql`.

Conclusión: base V001 suficientemente revisada para pasar a implementación cuando se decida.

## Actualización — Alineación aplicación autocontenida

Estado: completada.

Se corrigió la dirección arquitectónica para dejar claro que **Mi tienda de barrio admin** no será backend + frontend ni Spring Boot local.

Decisión actual:

```text
JavaFX + Core embebido + SQLite + reportes locales + respaldos locales + licencia local
```

Documentos añadidos:

- `docs/26-alineacion-aplicacion-autocontenida.md`
- `docs/27-cierre-alineacion-core-embebido.md`
- `core/README.md`
- `core/docs/00-indice-core.md`
- documentación específica del core embebido.
- `desktop/docs/18-integracion-javafx-core-embebido.md`
- `desktop/docs/19-aplicacion-autocontenida-arranque-runtime.md`
- `docs/adr/0009-aplicacion-autocontenida-v1.md`
- `docs/adr/0010-usar-core-embebido-en-lugar-de-backend.md`

Siguiente paso lógico:

```text
Tanda Core Embebido 1 — estructura base de aplicación autocontenida
```

No usar la palabra backend para la implementación V1. Solo usarla al hablar de transición futura.


## Tanda Core Embebido 1

Estado: iniciada.

Se creó estructura fuente mínima para aplicación autocontenida:

- `desktop/pom.xml`.
- `MiTiendaDeBarrioAdminApp`.
- bootstrap y contexto interno.
- core embebido base.
- infraestructura runtime/SQLite inicial.
- resultados y errores locales.
- componentes JavaFX reutilizables.
- loading, login y shell base.

No se implementaron todavía módulos de negocio completos.


## V9 — Configuración del negocio y UX transversal

Se agregó el primer módulo vertical conectado a core embebido y SQLite.

Decisiones:
- se mantiene aplicación autocontenida;
- se evita backend HTTP;
- se prioriza UX/UI transversal antes de módulos de negocio pesados;
- Configuración del negocio se usa como patrón de formulario + ayuda + guardado.

Documentos nuevos:
- `docs/29-tanda-configuracion-negocio-y-ux-transversal.md`
- `desktop/docs/22-plan-ux-ui-inteligente.md`
- `core/docs/15-configuracion-negocio-vertical-slice.md`
- `docs/30-tandas-pendientes-desde-v9.md`

## V10 — Catálogos base y carcasa parametrizable

Se implementó la primera tanda posterior a Configuración.

Incluye:

- carcasa parametrizable mediante `AppShellDescriptor` y `AppModuleDescriptor`;
- módulo Catálogos base;
- CRUD local de categorías;
- CRUD local de marcas;
- CRUD local de unidades de medida;
- servicios de aplicación;
- puertos de repositorio;
- adaptadores SQLite;
- vistas JavaFX por catálogo.

Criterios aplicados:

- aplicación autocontenida JavaFX + Core embebido + SQLite;
- nada de backend HTTP;
- responsabilidad única por clase;
- parametrización de carcasa para reutilización futura;
- desactivación lógica en lugar de eliminación física.

Documentos nuevos:

- `docs/31-tanda-catalogos-base-y-carcasa-parametrizable.md`
- `docs/32-tandas-pendientes-desde-v10.md`
- `core/docs/16-catalogos-base-vertical-slice.md`
- `desktop/docs/24-carcasa-parametrizable-y-catalogos.md`

## Estado desde v11

Se implementaron dos tandas adicionales:

- Tanda 2: Proveedores.
- Tanda 3: Productos / inventario base.

La aplicación mantiene el enfoque autocontenido y la carcasa parametrizable.

Siguiente paso recomendado: movimientos de inventario con transacciones y trazabilidad.


## V12 — Movimientos, compras y salidas

Se implementaron tres tandas verticales adicionales: movimientos de inventario, compras/entradas y salidas/ventas internas. Quedan pendientes reportes, respaldos, licencia, caja, fiado, ayuda contextual, PDF/pulido y empaquetado.

## Tanda v13 — Reportes, respaldos y licencia

Se agregaron tres vertical slices locales:

- Reportes operativos con exportación CSV.
- Respaldos/restauración de base SQLite.
- Licencia local básica con modo limitado ético.

Siguiente tanda recomendada: caja diaria opcional.

## Actualización V14 — Caja, fiado y ayuda contextual

Se completaron tres tandas adicionales:
- Caja diaria opcional.
- Fiado / cuentas por cobrar opcional.
- Ayuda contextual / mini manual interno.

La aplicación mantiene arquitectura autocontenida JavaFX + core embebido + SQLite.

## Actualización v15 — Dashboard, modo limitado y PDF básico

Se agregaron tres tandas nuevas:

- Tanda 13: Dashboard de inicio real.
- Tanda 14: Modo limitado de licencia aplicado inicialmente a navegación de módulos de escritura.
- Tanda 15: Exportación PDF básica para reportes operativos.

También se corrigió una inconsistencia detectada en el esquema V001: duplicidad de la columna `observacion` en `cuenta_por_cobrar`.

Siguiente recomendación: avanzar con integraciones internas, especialmente venta interna → caja y venta interna → fiado, antes de pulir UX/UI general.
