# Repositorios SQLite

## Reglas

- La UI no ejecuta SQL.
- Los repositorios encapsulan consultas y escrituras.
- Las operaciones de escritura participan en transacciones controladas.
- Los SQL bruto y 1FN son trazabilidad legacy/formal; la fuente física oficial es `database/sql/migrations/V001__schema_3fn_oficial.sql`.

## Repositorios sugeridos

```text
ProductoRepository
CategoriaRepository
MarcaRepository
UnidadMedidaRepository
ProveedorRepository
CompraRepository
VentaInternaRepository
MovimientoInventarioRepository
ReporteReadRepository
ConfiguracionRepository
LicenciaRepository
RespaldoRepository
```
