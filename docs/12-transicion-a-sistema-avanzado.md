# Transición a sistema avanzado

## Decisión base

La V1 de **Mi tienda de barrio admin** es una aplicación autocontenida:

```text
JavaFX + Core embebido + SQLite
```

La transición a sistema avanzado no es parte de V1. Se cotiza, diseña y desarrolla como otra etapa cuando la operación del cliente excede el modelo local de una computadora.

## Señales de transición

- Varias computadoras usando el sistema al mismo tiempo.
- Varios usuarios con permisos diferenciados reales.
- Varias sucursales.
- Acceso remoto desde casa, celular u otra sede.
- Facturación electrónica o integración SRI.
- Alta concurrencia.
- Roles, auditoría y trazabilidad fuerte.
- Tienda online.
- App móvil.
- Integración con balanzas, lectores de código de barras como requisito operativo o hardware especializado.
- Necesidad de respaldo centralizado automático.
- Riesgo alto si una sola computadora concentra toda la operación.

## Arquitectura siguiente

Cuando esas señales aparezcan, la arquitectura probable cambia a:

```text
Cliente JavaFX o web
→ Backend centralizado
→ PostgreSQL
→ servicios de integración / facturación / auditoría / backups
```

El backend avanzado puede ser Spring Boot u otra tecnología, pero eso queda fuera del producto autocontenido V1.

## Cómo comunicarlo comercialmente

> Esta versión local fue hecha para una computadora. Lo que pide ya requiere un sistema centralizado y corresponde a otra etapa con otro presupuesto.

Otra forma:

> Podemos conservar sus datos y migrarlos, pero ya no sería la versión local autocontenida. Ahí pasaríamos a una solución más robusta con base central y acceso para varias computadoras.

## Relación con SQLite

SQLite no se abandona porque sea malo. Se migra porque cambian las condiciones:

- de una computadora a varias;
- de operación local a operación remota;
- de control interno a integración tributaria;
- de usuario único a permisos/auditoría;
- de archivo local a disponibilidad central.

## Migración futura

La V1 debe dejar preparados:

- IDs estables;
- nombres de tablas claros;
- diccionario de datos;
- migración V001 documentada;
- exportación de datos;
- reportes de consistencia;
- separación conceptual de dominio y persistencia.
