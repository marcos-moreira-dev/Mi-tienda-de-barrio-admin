# Tanda 6 — Usuarios, roles y permisos locales

## Objetivo

Reemplazar el login hardcoded por una base mínima de usuarios, roles y permisos locales, manteniendo la filosofía offline de MiTienda ERP Local SQLite.

## Cambios de base de datos

Se agregaron a la V001 consolidada:

- `usuario_local`
- `rol_local`
- `permiso_local`
- `rol_permiso_local`
- `usuario_rol_local`

También se agregaron índices para usuario, roles y permisos.

## Seed inicial

El seed limpio crea:

- usuario `admin`;
- rol `ADMIN_LOCAL`;
- rol `OPERADOR_LOCAL`;
- rol `SOLO_LECTURA`;
- permisos locales base;
- relación `admin` → `ADMIN_LOCAL`.

La contraseña inicial sigue siendo `admin123456`, pero ahora se valida contra hash local en SQLite. El seed marca `debe_cambiar_clave = 1` para indicar que debe cambiarse antes de usar el sistema con un negocio real.

## Cambios de código

Se agregaron paquetes de seguridad local:

- `core/domain/seguridad`
- `core/application/seguridad`
- `core/infrastructure/seguridad`

Clases principales:

- `UsuarioLocalService`
- `UsuarioLocalRepository`
- `SqliteUsuarioLocalRepository`
- `UsuarioLocalCredenciales`
- `SesionUsuarioLocal`
- `EstadoUsuarioLocal`

`AppBootstrap` ahora construye `UsuarioLocalService`.

`AppContext` ahora expone `usuarioLocalService()`.

`LoginView` ya no compara la contraseña de forma hardcodeada. Ahora llama:

```java
context.usuarioLocalService().autenticar(user, password)
```

## Validación

Se agregó:

- `scripts/validate-usuarios-locales.bat`
- `scripts/validate-usuarios-locales.ps1`

La validación revisa:

- tablas locales de usuarios;
- seed del usuario admin;
- hash de `admin123456`;
- rol `ADMIN_LOCAL`;
- permisos mínimos;
- conexión de `LoginView` con `UsuarioLocalService`;
- claves foráneas.

## Decisión

Esta tanda no implementa una administración completa de usuarios en pantalla. Solo deja la infraestructura mínima para dejar atrás el login hardcoded.

La pantalla de administración de usuarios queda para una tanda posterior.
