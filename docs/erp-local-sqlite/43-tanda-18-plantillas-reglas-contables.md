# Tanda 18 — Plantillas y reglas contables

## Decisión

Esta tanda agrega una capa mínima de plantillas y reglas contables para generar asientos sugeridos desde eventos operativos comunes.

No convierte el sistema en contabilidad automática completa. La automatización queda controlada y limitada a plantillas simples de dos líneas: una línea en debe y una línea en haber.

## Archivos agregados

- `LadoPlantillaAsiento.java`
- `PlantillaAsiento.java`
- `PlantillaAsientoDetalle.java`
- `ReglaContableEvento.java`
- `CrearAsientoDesdePlantillaSolicitud.java`

## Archivos modificados

- `ContabilidadBasicaService.java`
- `ContabilidadBasicaRepository.java`
- `SqliteContabilidadBasicaRepository.java`
- `V001__seed_inicial_cliente.sql`
- `V001__smoke_check.sql`
- `test.bat`

## Plantillas base

- `VENTA_PAGADA`
- `VENTA_FIADA`
- `ABONO_FIADO`
- `COMPRA_PAGADA`
- `COMPRA_CREDITO`
- `PAGO_PROVEEDOR`
- `GASTO_OPERATIVO`
- `MERMA_INVENTARIO`
- `CAPITAL_INICIAL`
- `AJUSTE_GENERAL`

## Reglas base

Cada evento operativo queda asociado a una plantilla contable sugerida mediante `regla_contable_evento`.

Ejemplo:

- evento `VENTA_PAGADA` → plantilla `VENTA_PAGADA`
- evento `ABONO_FIADO` → plantilla `ABONO_FIADO`
- evento `GASTO_OPERATIVO` → plantilla `GASTO_OPERATIVO`

## Alcance

El sistema puede listar plantillas, listar reglas y registrar un asiento desde una plantilla o desde un evento contable.

La validación exige:

- importe mayor que cero;
- plantilla activa;
- una línea en debe;
- una línea en haber;
- asiento cuadrado.

## Validación

Se agrega:

- `scripts/validate-contabilidad-reglas.bat`
- `scripts/validate-contabilidad-reglas.ps1`

El `test.bat` general ahora ejecuta también esta validación.

