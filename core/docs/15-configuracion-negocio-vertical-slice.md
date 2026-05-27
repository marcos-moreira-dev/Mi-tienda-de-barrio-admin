# Vertical slice — Configuración del negocio

## Propósito

Probar una rebanada vertical real de la aplicación autocontenida:

```text
UI JavaFX → Core embebido → Repositorio SQLite → V001
```

## Entidad

`ConfiguracionNegocio`

Campos:

- nombre comercial;
- RUC;
- responsable;
- teléfono;
- dirección;
- actividad;
- moneda;
- observación.

## Reglas

- nombre comercial obligatorio;
- moneda obligatoria;
- la pantalla no reemplaza asesoría legal, tributaria ni municipal;
- se guarda un único registro porque V1 no es multiempresa.

## Puerto

`ConfiguracionNegocioRepository`

## Adaptador

`SqliteConfiguracionNegocioRepository`

## Servicio

`ConfiguracionNegocioService`

## Pantalla

`ConfiguracionNegocioView`

## Criterio de cierre

Se considera cerrada cuando:

- carga configuración existente o valores vacíos;
- permite editar;
- valida mínimos;
- guarda en SQLite;
- muestra feedback claro;
- mantiene separación entre UI, core e infraestructura.
