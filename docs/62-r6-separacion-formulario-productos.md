# R6 – Separación inicial de formularios grandes

## Objetivo

Evitar que las pantallas JavaFX grandes mezclen demasiadas responsabilidades en una sola clase.

La primera pantalla separada fue `ProductosInventarioView`, porque concentra filtros, lista, formulario, lectura de campos, escritura de campos y acciones de guardado/cambio de estado.

## Antes

`ProductosInventarioView` hacía todo:

- construir filtros;
- construir lista;
- construir formulario;
- configurar combos;
- llenar campos;
- limpiar campos;
- convertir texto a números;
- guardar;
- cambiar estado.

## Después

Se agregaron piezas específicas:

```text
desktop/ui/screens/productos/
  ProductoFilterPane.java
  ProductoListPane.java
  ProductoFormPane.java
  ProductoFormData.java
```

`ProductosInventarioView` queda como orquestador de pantalla:

- carga catálogos;
- carga datos;
- coordina selección;
- llama servicios;
- muestra diálogos.

## Decisiones

- No se cambió el diseño funcional.
- No se cambió la lógica de negocio.
- No se movió lógica a servicios nuevos innecesarios.
- `ProductoFormData` es solo una estructura de transporte de datos capturados por UI.
- La conversión a `Producto` se mantiene explícita y visible.

## Beneficio

La pantalla queda preparada para mejoras posteriores:

- validaciones visuales por campo;
- scroll interno del formulario;
- modo lectura/escritura;
- reutilización parcial del formulario para wizard o carga rápida;
- pruebas manuales más claras.

## Siguiente paso recomendado

Aplicar el mismo patrón de forma gradual a:

1. `ComprasEntradasView`
2. `VentasInternasView`
3. `MovimientosInventarioView`
4. `FiadoCuentasView`
5. `CajaDiariaView`

No conviene refactorizar todas en una sola tanda sin compilar entre pasos.
