# Tanda 9 — Licencia local

## Objetivo

Implementar una licencia local renovable y ética para proteger el modelo comercial sin secuestrar los datos del cliente.

## Alcance implementado

- Lectura de licencia desde SQLite.
- Activación/renovación con código y fecha de vencimiento.
- Estado ACTIVA.
- Periodo de gracia.
- Modo limitado cuando vence fuera del periodo de gracia.
- Pantalla de licencia.

## Principio comercial

La licencia no debe borrar ni bloquear los datos del cliente. La estrategia correcta es limitar nuevas operaciones si aplica, manteniendo consulta, respaldo y exportación.

## Pendiente de endurecimiento

El código de licencia todavía es básico. En una tanda posterior puede firmarse criptográficamente o verificarse por hash/firma local.
