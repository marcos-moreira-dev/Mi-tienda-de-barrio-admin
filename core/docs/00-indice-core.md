# Índice — Core embebido

## Propósito

Documentar el núcleo interno de la aplicación autocontenida.

## Archivos

- `01-vision-core-embebido.md`
- `02-arquitectura-core-embebido.md`
- `03-servicios-aplicacion-y-casos-de-uso.md`
- `04-dominio-entidades-reglas.md`
- `05-puertos-adaptadores-locales.md`
- `06-repositorios-sqlite.md`
- `07-transacciones-sqlite.md`
- `08-resultados-errores-locales.md`
- `09-configuracion-local.md`
- `10-reportes-exportaciones.md`
- `11-respaldos-restauracion.md`
- `12-licencia-local.md`
- `13-testing-core.md`
- `99-estado-core.md`

## Regla

El core debe poder probarse sin abrir pantallas JavaFX siempre que sea razonable. La UI consume casos de uso; no contiene reglas de negocio fuertes.
