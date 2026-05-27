# Presentación comercial

Esta carpeta separa la presentación comercial de la instalación real del cliente.

La instalación real debe iniciar limpia: sin productos, ventas, compras, caja ni clientes inventados. Para mostrar el sistema con datos cargados se usa una base separada de presentación.

## Flujo recomendado

1. Ejecutar `scripts\db-seed-presentacion.bat`.
2. Abrir la app con `scripts\dev-desktop-presentacion.bat`.
3. Tomar capturas siguiendo `checklist-capturas.md`.
4. Guardar capturas en `presentacion\capturas`.
5. Usar `guion-comercial.md` como apoyo para explicar el producto.

## Regla importante

No ejecutar scripts de presentación sobre datos reales de un cliente. El script oficial usa una carpeta separada:

```text
%USERPROFILE%\.mi-tienda-de-barrio-admin-presentacion
```
