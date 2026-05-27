# Tanda 16 — Fiscalidad preparada

## Decisión

Se implementa fiscalidad preparada local, no facturación electrónica real ni autorización SRI.

El sistema puede registrar documentos internos/preparados con tipos locales, identificación, impuestos configurables, detalle y advertencia visible.

## Archivos principales

- `tipo_identificacion_local`
- `tipo_comprobante_local`
- `impuesto_configuracion`
- `documento_fiscal_preparado`
- `documento_fiscal_preparado_detalle`

## Código agregado

- `core/domain/fiscalidad/*`
- `core/application/fiscalidad/*`
- `core/infrastructure/fiscalidad/*`

## Reglas

- Ningún documento preparado reemplaza comprobante autorizado por el SRI.
- No se emite, firma ni autoriza electrónicamente.
- Los impuestos son configurables; no deben tratarse como valores oficiales quemados en código.
- Toda operación de documento preparado pasa por licencia y auditoría.

## Validación

Scripts:

- `scripts/validate-fiscalidad-preparada.bat`
- `scripts/validate-fiscalidad-preparada.ps1`

Además se agregó un punto único de ejecución:

- `test.bat`
- `scripts/test.bat`

`test.bat` ejecuta todas las validaciones rápidas implementadas hasta esta tanda.
