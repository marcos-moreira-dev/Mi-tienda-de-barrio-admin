# Estabilización - tandas 2 a 4

## Tanda 2 - Modo cliente real y presentación separada

Se separó el concepto de instalación real de cliente y datos inventados de presentación.

### Decisión

La instalación real inicia limpia, pero operativamente útil:

- configuración base editable;
- licencia pendiente;
- categorías comunes;
- marca `Sin marca`;
- proveedor `Proveedor no especificado`;
- unidades de medida comunes;
- parámetros base;
- ayuda contextual.

No se cargan por defecto:

- productos inventados;
- ventas inventadas;
- compras inventadas;
- caja inventada;
- clientes de fiado inventados;
- movimientos inventados.

### Archivos agregados

- `database/sql/seeds/V001__seed_inicial_cliente.sql`
- `database/sql/seeds/V001__seed_presentacion.sql`
- `database/sql/seeds/V001__reset_presentacion.sql`
- `desktop/src/main/resources/db/seeds/V001__seed_inicial_cliente.sql`
- `desktop/src/main/resources/db/seeds/V001__seed_presentacion.sql`
- `desktop/src/main/resources/db/seeds/V001__reset_presentacion.sql`
- `scripts/db-seed-inicial-cliente.bat`
- `scripts/db-seed-presentacion.bat`
- `scripts/db-reset-presentacion.bat`
- `scripts/dev-desktop-presentacion.bat`
- `scripts/open-runtime-data.bat`
- `presentacion/README.md`
- `presentacion/checklist-capturas.md`
- `presentacion/guion-comercial.md`

### Nota técnica

La app ejecuta automáticamente el seed inicial de cliente de forma idempotente después de la migración. El seed de presentación no se ejecuta automáticamente.

## Tanda 3 - Login local serio

Se ajustó el login local para validar las credenciales iniciales:

```text
Usuario: admin
Contraseña: admin123456
```

El acceso sigue siendo local y simple. No se presenta como seguridad avanzada ni como módulo de roles.

Archivo modificado:

- `desktop/src/main/java/com/marcosmoreira/mitiendadebarrio/admin/desktop/ui/screens/login/LoginView.java`

## Tanda 4 - Licencia en profundidad para escrituras

Se aplicó `WriteAccessGuard` en servicios de aplicación que escriben datos:

- configuración del negocio;
- categorías;
- marcas;
- unidades de medida;
- proveedores;
- productos;
- movimientos de inventario;
- compras;
- ventas internas;
- caja diaria;
- fiado/cuentas por cobrar.

La regla de producto queda así:

- consulta permitida;
- exportación permitida;
- reportes permitidos;
- respaldos permitidos;
- escritura bloqueada si la licencia está en modo limitado.

## Verificaciones realizadas

- SQL de esquema, seed inicial, seed de presentación y reset de presentación validado con SQLite en memoria.
- Compilación parcial con `javac --release 21` de capas core/bootstrap/tools sin JavaFX.
- No se pudo ejecutar Maven completo en este entorno porque no está instalado.

## Siguiente tanda recomendada

Tanda 5: smoke funcional completo desde Windows:

1. ejecutar `scripts\dev-desktop.bat`;
2. verificar primer arranque limpio;
3. ingresar con `admin / admin123456`;
4. crear producto real de prueba;
5. registrar compra;
6. registrar venta interna;
7. probar caja;
8. probar fiado;
9. generar reporte;
10. generar respaldo;
11. ejecutar presentación separada con `scripts\db-seed-presentacion.bat` y `scripts\dev-desktop-presentacion.bat`.
