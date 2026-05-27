# Tanda 7 — Auditoría local mínima

## Decisión

Se agregó una bitácora local mínima para registrar acciones relevantes sin depender de nube, backend ni PostgreSQL.

La auditoría local no debe volver frágil la aplicación: si falla el registro de auditoría en una operación normal, la operación no debe caerse. Las operaciones críticas podrán endurecer esta política más adelante.

## Cambios principales

### Base de datos

Se agregó la tabla:

```text
auditoria_evento
```

Campos principales:

```text
usuario_id
fecha_evento
modulo
accion
entidad
entidad_id
resumen
detalle_json
resultado
```

Resultados permitidos:

```text
OK
ADVERTENCIA
ERROR
```

También se agregaron índices por fecha, usuario, módulo/acción y entidad.

### Código

Se agregaron paquetes:

```text
core/domain/auditoria
core/application/auditoria
core/infrastructure/auditoria
```

Clases principales:

```text
AuditoriaEvento
ResultadoAuditoria
AuditoriaRepository
AuditoriaService
SqliteAuditoriaRepository
```

### Integración inicial

Se integró auditoría en:

```text
AppBootstrap
AppContext
UsuarioLocalService
```

El login local registra:

```text
LOGIN_CORRECTO
LOGIN_FALLIDO
LOGIN_BLOQUEADO
LOGIN_ERROR_ALGORITMO
```

No se registra la contraseña ni datos sensibles.

## Validación

Se agregó:

```text
scripts/validate-auditoria-local.bat
scripts/validate-auditoria-local.ps1
```

El script valida:

```text
tabla auditoria_evento
inserción de evento de prueba
claves foráneas
conexión con AppBootstrap/AppContext
login auditado en UsuarioLocalService
repositorio SQLite de auditoría
```

## Pendiente

En tandas posteriores se conectará auditoría con operaciones críticas:

```text
productos
compras
ventas
caja
fiado
respaldos
restauración
licencia
contabilidad
```

