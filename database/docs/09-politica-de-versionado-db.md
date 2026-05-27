# Política de versionado DB

## Objetivo

Asegurar que cada instalación local sepa qué versión de base de datos tiene y cómo actualizarse.

## Reglas principales

1. Toda versión de app debe declarar versión mínima y máxima de esquema soportada.
2. Toda migración debe ser irreversible solo si existe respaldo previo.
3. Toda actualización debe crear backup antes de modificar datos.
4. No aplicar migraciones si la base está en una versión desconocida.
5. No saltar migraciones intermedias.

## Versiones

Ejemplo:

```text
app_version = 1.0.0
schema_version = 1
```

## Estrategia

- V001: base mínima de productos, proveedores, compras, ventas internas, movimientos, reportes, licencia y respaldo.
- V002: ajustes por feedback de campo.
- V003: caja diaria si se activa como módulo físico.
- V004: fiado/cuentas por cobrar si se activa como módulo físico.

## Validación de arranque

Al abrir la aplicación:

1. verificar que existe base;
2. verificar versión;
3. verificar integridad básica;
4. si falta migración, pedir respaldo y aplicar;
5. si hay error, no abrir operación normal hasta resolver.
