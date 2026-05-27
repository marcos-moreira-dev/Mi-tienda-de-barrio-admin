# Tanda 19 — Opcionales mínimos

## Decisión

Se agregan módulos opcionales mínimos para que MiTienda tenga cobertura ERP local sin convertirse en un sistema pesado de nómina, BI, activos fijos formales o ETL empresarial.

## Implementado

- Activos mínimos: `tipo_activo_negocio`, `activo_negocio`.
- RRHH mínimo: `cargo_empleado`, `empleado_local`.
- Indicadores operativos: `indicador_operativo`, `consulta_reporte_log`.
- Importaciones CSV: `plantilla_importacion`, `lote_importacion`, `error_importacion`.
- Checklist operativo: `checklist_operativo`, `checklist_item`.

## Código agregado

- `core/domain/opcional`.
- `core/application/opcional`.
- `core/infrastructure/opcional`.

## Alcance

Estos módulos son mínimos. No implementan nómina, depreciación contable formal, BI empresarial ni importaciones complejas.

## Validación

Ejecutar:

```powershell
.\test.bat
```

O solo esta tanda:

```powershell
.\scripts\validate-opcionales-minimos.bat
```
