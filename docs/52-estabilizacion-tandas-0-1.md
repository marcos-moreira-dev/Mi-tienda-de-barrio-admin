# Estabilización - tandas 0 y 1

## Objetivo

Dejar el proyecto en una base más cercana a V1 estabilizable, sin cambiar el alcance comercial del producto.

El criterio de producto se mantiene:

- instalación limpia para cliente real;
- datos de presentación separados y opcionales;
- no usar el concepto de demo en nombres visibles ni en código;
- caja y fiado como módulos opcionales;
- licencia ética: consultar, exportar, reportar y respaldar siguen disponibles aunque se limite la escritura.

## Tanda 0 - Compilación base

### Problema encontrado

`FiadoService.java` estaba vacío. Maven no podía compilar porque `AppContext`, `AppBootstrap` y `FiadoCuentasView` dependían de la clase `FiadoService`.

### Cambio aplicado

Se implementó `FiadoService` como servicio de aplicación para:

- buscar clientes de fiado;
- listar cuentas abiertas;
- guardar cliente de fiado;
- abrir cuenta por cobrar;
- registrar abono;
- desactivar/reactivar cliente desde servicio.

La implementación valida entradas básicas y devuelve `OperationResult` como el resto de servicios de aplicación.

## Tanda 1 - Arranque limpio y ayuda contextual

### Problema encontrado

La migración oficial `V001__schema_3fn_oficial.sql` define `ayuda_contextual` con columnas:

- `modulo`;
- `clave`;
- `titulo`;
- `contenido`;
- `estado`.

Pero `SqliteAyudaContextualRepository` consultaba columnas que no existen en ese esquema:

- `orden`;
- `visible`.

Esto podía permitir compilar, pero romper el arranque al ejecutar `ayudaContextualService.asegurarContenidoBase()`.

### Cambio aplicado

Se alineó `SqliteAyudaContextualRepository` con el esquema real:

- ahora filtra por `estado = 'ACTIVA'`;
- ahora inserta contenido base usando `modulo`, `clave`, `titulo`, `contenido` y `estado`;
- conserva el contrato `AyudaContextual` derivando `orden` por orden de lectura y `visible = true`.

## Verificación realizada en entorno de trabajo

- Se compiló con `javac` el bloque core/bootstrap sin JavaFX: correcto.
- Se validó con SQLite en memoria, usando Python, que el esquema oficial permite insertar y consultar `ayuda_contextual` con las columnas reales.

No se pudo ejecutar Maven completo en este entorno porque no está instalado `mvn` aquí. La verificación final recomendada en Windows es:

```bat
cd desktop
mvn clean compile
mvn javafx:run
```

o desde la raíz:

```bat
scripts\dev-desktop.bat
```

## Pendientes inmediatos después de estas tandas

1. Ejecutar Maven en Windows con JDK 21.
2. Arrancar con base limpia.
3. Hacer smoke manual de módulos.
4. Separar formalmente seed inicial de cliente y seed de presentación.
5. Implementar licencia en profundidad dentro de servicios de escritura.
