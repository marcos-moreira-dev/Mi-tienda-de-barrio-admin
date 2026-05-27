# Servicios de aplicación y casos de uso

Los casos de uso representan acciones del negocio, no botones.

Ejemplos:

- registrar producto;
- registrar compra;
- registrar venta interna;
- registrar merma;
- generar reporte de productos por comprar;
- crear respaldo;
- renovar licencia.

## Convención sugerida

```text
RegistrarProductoUseCase
ActualizarProductoUseCase
RegistrarCompraUseCase
RegistrarVentaInternaUseCase
GenerarReporteProductosPorComprarUseCase
CrearRespaldoUseCase
ValidarLicenciaUseCase
```

## Regla

Los controladores JavaFX no deben construir SQL ni decidir reglas de stock. Deben llamar a casos de uso o servicios de aplicación.
