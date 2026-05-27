# Tanda 2 — Proveedores

## Objetivo

Implementar el módulo vertical de proveedores dentro de la aplicación autocontenida, manteniendo el flujo:

```text
JavaFX → Core embebido → Repository → SQLite
```

## Decisiones aplicadas

- El módulo no se diseñó como contabilidad ni cuentas por pagar.
- Proveedor es una entidad formal porque alimenta compras, productos, historial de costos y reportes.
- Se permite desactivar proveedores sin borrarlos, para conservar trazabilidad.
- La observación es deliberadamente amplia porque en tiendas pequeñas existen acuerdos informales, rutas de visita, cambios de precio y condiciones variables.

## Archivos principales

```text
desktop/src/main/java/.../core/domain/proveedor/Proveedor.java
desktop/src/main/java/.../core/domain/proveedor/EstadoProveedor.java
desktop/src/main/java/.../core/application/proveedor/ProveedorRepository.java
desktop/src/main/java/.../core/application/proveedor/ProveedorService.java
desktop/src/main/java/.../core/infrastructure/proveedor/SqliteProveedorRepository.java
desktop/src/main/java/.../desktop/ui/screens/proveedores/ProveedoresView.java
```

## Responsabilidad única

- `Proveedor` representa datos de dominio.
- `ProveedorService` valida y coordina casos de uso.
- `ProveedorRepository` define el puerto.
- `SqliteProveedorRepository` habla con SQLite.
- `ProveedoresView` representa la pantalla JavaFX del módulo.

## Alcance funcional

- Listar proveedores activos o incluir inactivos.
- Crear proveedor.
- Editar proveedor.
- Activar/desactivar proveedor.
- Guardar teléfono, WhatsApp, dirección y observación.

## Pendiente futuro

- Historial de compras por proveedor.
- Incidencias por proveedor.
- Comparación de precio anterior vs precio actual.
