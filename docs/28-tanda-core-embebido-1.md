# Tanda Core Embebido 1 — Base autocontenida JavaFX + Core + SQLite

## Propósito

Esta tanda inicia la estructura fuente real de **Mi tienda de barrio admin** como aplicación autocontenida.

La decisión oficial es:

```text
JavaFX + Core embebido + SQLite
```

No existe backend HTTP, servidor local, API REST ni Spring Boot en V1.

## Revisión previa

La documentación y la base de datos V001 ya estaban avanzadas, pero faltaba una estructura fuente mínima que materialice la arquitectura autocontenida. Esta tanda cierra esa brecha.

## Recomendación de orden

No conviene implementar primero “todo el core” ni primero “toda la UI”. El orden sano es híbrido:

1. Transversales JavaFX mínimos: arranque, loading, shell, login, tema, componentes visuales y diálogos.
2. Core embebido mínimo: resultados, errores, runtime local, configuración, conexión SQLite y transacciones.
3. Primer módulo vertical: Configuración del negocio o Productos.
4. Repetir patrón por módulo: dominio, caso de uso, repositorio, pantalla, validaciones y pruebas.

## Resultado de esta tanda

Queda creada la primera base de código, todavía sin reglas de negocio completas de productos, compras, ventas o reportes.

## Definition of Done

- El proyecto declara explícitamente que es autocontenido.
- La UI JavaFX tiene arranque mínimo documentado.
- El core embebido existe como paquete interno, no como servidor.
- Los resultados y errores locales reemplazan contratos HTTP.
- SQLite aparece como infraestructura local.
- Los componentes customizados empiezan antes de las pantallas de negocio.
