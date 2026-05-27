# Puertos y adaptadores locales

## Puertos internos

```text
ProductoRepository
ProveedorRepository
CompraRepository
InventarioRepository
ReporteExporter
BackupService
LicenseService
ClockProvider
FileStorage
```

## Adaptadores locales

```text
SqliteProductoRepository
SqliteProveedorRepository
PdfReporteExporter
CsvReporteExporter
LocalBackupService
SignedFileLicenseService
SystemClockProvider
LocalFileStorage
```

Aunque la aplicación sea autocontenida, esta separación permite probar lógica sin JavaFX, cambiar reportes, migrar en el futuro y evitar SQL disperso.
