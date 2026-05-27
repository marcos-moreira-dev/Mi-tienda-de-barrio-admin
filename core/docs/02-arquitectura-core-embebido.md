# Arquitectura del core embebido

## Flujo

```text
UI JavaFX
→ Controller
→ Command / Query
→ UseCase / ApplicationService
→ Domain
→ Repository Port
→ SQLite Adapter
```

## Capas internas

- **UI:** recibe eventos, muestra datos y traduce resultados.
- **Application:** orquesta casos de uso y transacciones.
- **Domain:** entidades, value objects, enums y reglas.
- **Infrastructure:** repositorios SQLite, reportes, backups, licencia, configuración, logs y archivos.
- **Shared:** resultados, errores, paginación local y utilidades transversales.

## Regla de dependencia

```text
UI depende de Application.
Application depende de Domain y puertos.
Infrastructure implementa puertos.
Domain no depende de UI ni Infrastructure.
```

No se debe crear una arquitectura exagerada; se permite agrupar clases cuando el módulo sea simple.
