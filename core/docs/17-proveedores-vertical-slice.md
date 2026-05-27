# Proveedores — vertical slice

El módulo Proveedores prueba que el core embebido puede incorporar una entidad nueva sin romper la carcasa ni duplicar responsabilidades.

## Flujo

```text
ProveedoresView
→ ProveedorService
→ ProveedorRepository
→ SqliteProveedorRepository
→ proveedor
```

## Reglas

- Nombre obligatorio.
- Teléfono, WhatsApp, dirección y observación son opcionales.
- El proveedor no se elimina físicamente; se desactiva.

## Uso futuro

Proveedor alimentará compras, productos, historial de costos y reportes.
