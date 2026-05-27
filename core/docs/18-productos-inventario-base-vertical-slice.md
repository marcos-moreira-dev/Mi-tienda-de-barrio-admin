# Productos / Inventario base — vertical slice

El módulo Productos introduce el catálogo maestro que luego será usado por compras, salidas, movimientos, reportes, respaldos y auditoría simple.

## Flujo

```text
ProductosInventarioView
→ ProductoService
→ ProductoRepository
→ SqliteProductoRepository
→ producto + categoria + marca + unidad_medida + proveedor
```

## Reglas

- Nombre obligatorio.
- Categoría obligatoria.
- Unidad de medida obligatoria.
- Stock y precios no pueden ser negativos.
- Stock objetivo no puede ser menor que stock mínimo.
- Lote/vencimiento/perecible/refrigerado son capacidades configurables.

## Pendiente

Este módulo todavía no debe reemplazar movimientos de inventario ni compras. Solo define el maestro.
