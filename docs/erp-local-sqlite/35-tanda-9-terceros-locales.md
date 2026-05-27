# Tanda 9 — Terceros locales

## Objetivo

Agregar la base técnica para manejar clientes y proveedores bajo un modelo unificado de terceros, sin mostrar jerga innecesaria al usuario final.

## Decisión

MiTienda conserva su identidad local SQLite. Esta tanda no reemplaza todavía todas las pantallas existentes de proveedores/fiado, pero deja el núcleo listo para que las siguientes tandas migren gradualmente hacia clientes y proveedores unificados.

## Tablas agregadas

- `tercero`
- `cliente_perfil`
- `proveedor_perfil`
- `tercero_contacto`
- `tercero_direccion`

## Código agregado

Paquetes nuevos:

- `core/domain/tercero`
- `core/application/tercero`
- `core/infrastructure/tercero`

Clases principales:

- `Tercero`
- `TipoTercero`
- `EstadoTercero`
- `TerceroContacto`
- `TerceroDireccion`
- `TerceroRepository`
- `TerceroService`
- `SqliteTerceroRepository`

## Integración

Se agregó `TerceroService` al `AppContext` y se construye desde `AppBootstrap` con `SqliteTerceroRepository`, `WriteAccessGuard` y `AuditoriaService`.

## Seed

Se agregaron registros base:

- Consumidor final local.
- Proveedor no especificado.

También se agregó permiso local:

- `TERCEROS_OPERAR`.

## Validación

Se agregó:

- `scripts/validate-terceros-locales.bat`
- `scripts/validate-terceros-locales.ps1`

El script valida tablas, seed, perfiles, contactos, direcciones, claves foráneas e integración Java.

## Límites de esta tanda

Esta tanda no rehace todavía las pantallas de proveedores ni fiado. Tampoco elimina las tablas antiguas `proveedor` y `cliente_fiado`. La migración funcional completa se hará en tandas posteriores para no romper la operación actual.
