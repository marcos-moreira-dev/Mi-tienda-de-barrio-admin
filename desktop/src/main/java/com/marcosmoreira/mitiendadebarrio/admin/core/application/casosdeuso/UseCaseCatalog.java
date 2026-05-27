package com.marcosmoreira.mitiendadebarrio.admin.core.application.casosdeuso;

import java.util.List;

/** Catálogo documentado de casos de uso del sistema de tienda. */
public final class UseCaseCatalog {
    private UseCaseCatalog() {
    }

    public static List<UseCaseModule> modules() {
        return List.of(
                new UseCaseModule("Inicio", "Resumen operativo, alertas y lectura rápida del negocio.", List.of(
                        new UseCaseItem("CU-INI-01", "Inicio", "Abrir el escritorio y revisar alertas", "Ver en una sola pantalla productos activos, bajo stock, agotados, compras, ventas internas, caja y último respaldo.", "Módulo Inicio.", List.of("Abrir la aplicación.", "Ingresar con admin / admin123456.", "Entrar a Inicio.", "Revisar métricas y acciones sugeridas."), "El dueño identifica lo urgente sin entrar módulo por módulo."),
                        new UseCaseItem("CU-INI-02", "Inicio", "Validar licencia y modo local", "Confirmar que la app trabaja localmente con SQLite y que la licencia está disponible o limitada.", "Módulo Inicio y badge superior.", List.of("Abrir Inicio.", "Revisar chips Modo local, SQLite y Licencia.", "Ir a Licencia si aparece No configurada o limitada."), "El operador sabe si puede registrar datos o solo consultar/exportar.")
                )),
                new UseCaseModule("Configuración", "Identidad del negocio y datos base de reportes.", List.of(
                        new UseCaseItem("CU-CON-01", "Configuración", "Configurar datos reales del negocio", "Registrar nombre comercial, responsable, teléfono, dirección, actividad y moneda para reportes y operación.", "Módulo Configuración.", List.of("Entrar a Configuración.", "Colocar nombre comercial y moneda.", "Registrar contacto y dirección.", "Guardar configuración.", "Volver a Inicio para validar que el sistema ya no luce genérico."), "Los reportes y la app quedan contextualizados para el cliente real."),
                        new UseCaseItem("CU-CON-02", "Configuración", "Preparar base limpia para cliente", "Separar datos reales de datos de presentación para no mezclar operación con demostración.", "Scripts de base limpia y modo presentación.", List.of("Ejecutar seed inicial de cliente si hace falta.", "Usar seed de presentación solo en runtime separado.", "Verificar que el cliente real no tenga productos inventados."), "El repositorio conserva capacidad de demo sin ensuciar la operación real.")
                )),
                new UseCaseModule("Catálogos", "Categorías, marcas y unidades reutilizables.", List.of(
                        new UseCaseItem("CU-CAT-01", "Catálogos", "Registrar categoría", "Crear categorías de productos como Víveres, Lácteos, Limpieza o Bebidas.", "Módulo Catálogos, pestaña Categorías.", List.of("Entrar a Catálogos.", "Abrir pestaña Categorías.", "Escribir nombre y descripción.", "Guardar categoría."), "La categoría queda disponible para clasificar productos y filtrar reportes."),
                        new UseCaseItem("CU-CAT-02", "Catálogos", "Registrar unidad de medida", "Definir unidades como unidad, funda, caja, litro, kilo o paquete.", "Módulo Catálogos, pestaña Unidades.", List.of("Entrar a Catálogos.", "Abrir pestaña Unidades.", "Registrar nombre y símbolo si aplica.", "Guardar unidad."), "Los productos pueden manejar stock con una unidad entendible para el negocio."),
                        new UseCaseItem("CU-CAT-03", "Catálogos", "Desactivar catálogo sin borrar trazabilidad", "Quitar de uso una categoría, marca o unidad sin eliminar historial.", "Módulo Catálogos.", List.of("Seleccionar elemento existente.", "Presionar Activar / desactivar.", "Confirmar que ya no aparezca como opción principal."), "El sistema mantiene trazabilidad y evita datos huérfanos.")
                )),
                new UseCaseModule("Proveedores", "Contactos y fuentes de abastecimiento.", List.of(
                        new UseCaseItem("CU-PROV-01", "Proveedores", "Registrar proveedor", "Guardar nombre, teléfono, WhatsApp, dirección y observaciones de una fuente de mercadería.", "Módulo Proveedores.", List.of("Entrar a Proveedores.", "Presionar Nuevo.", "Completar nombre obligatorio.", "Agregar teléfono o WhatsApp si existe.", "Guardar proveedor."), "El proveedor queda disponible para compras y trazabilidad de costos."),
                        new UseCaseItem("CU-PROV-02", "Proveedores", "Desactivar proveedor", "Retirar un proveedor de operación sin borrar compras anteriores.", "Módulo Proveedores.", List.of("Seleccionar proveedor.", "Presionar Activar / desactivar.", "Revisar que no se use como proveedor principal."), "La tienda conserva historial y evita usar proveedores inactivos.")
                )),
                new UseCaseModule("Productos", "Catálogo maestro, stock mínimo, stock objetivo y alertas.", List.of(
                        new UseCaseItem("CU-PROD-01", "Productos", "Registrar producto vendible", "Crear un producto con categoría, unidad, precio, stock y criterios de reposición.", "Módulo Productos.", List.of("Entrar a Productos.", "Presionar Nuevo.", "Completar nombre, categoría y unidad.", "Registrar precio de venta, stock actual, stock mínimo y stock objetivo.", "Guardar producto."), "El producto queda listo para compras, salidas, movimientos y reportes."),
                        new UseCaseItem("CU-PROD-02", "Productos", "Buscar o filtrar producto", "Encontrar rápidamente un producto por nombre, código o estado.", "Módulo Productos.", List.of("Entrar a Productos.", "Escribir texto en búsqueda.", "Presionar Buscar.", "Activar Mostrar inactivos si se revisa historial."), "El operador localiza productos sin recorrer todo el catálogo."),
                        new UseCaseItem("CU-PROD-03", "Productos", "Marcar perecible o refrigerado", "Indicar productos que requieren control especial por vencimiento o conservación.", "Módulo Productos.", List.of("Seleccionar producto.", "Activar Perecible, Refrigerado o Maneja vencimiento.", "Guardar cambios."), "El producto queda preparado para reportes de vencimiento y control operativo.")
                )),
                new UseCaseModule("Compras", "Entradas de mercadería, costo, lote y vencimiento.", List.of(
                        new UseCaseItem("CU-COM-01", "Compras", "Registrar entrada de mercadería", "Aumentar stock al recibir productos comprados a un proveedor.", "Módulo Compras.", List.of("Entrar a Compras.", "Seleccionar proveedor.", "Seleccionar producto.", "Ingresar cantidad y costo unitario.", "Registrar entrada."), "El stock aumenta y queda movimiento ENTRADA_COMPRA."),
                        new UseCaseItem("CU-COM-02", "Compras", "Registrar lote y vencimiento", "Guardar trazabilidad de lote cuando el producto lo requiere.", "Módulo Compras.", List.of("Seleccionar producto que maneja lote/vencimiento.", "Ingresar código de lote.", "Seleccionar fecha de vencimiento.", "Registrar entrada."), "La entrada queda preparada para reportes de productos próximos a vencer."),
                        new UseCaseItem("CU-COM-03", "Compras", "Actualizar costo referencial", "Usar el costo de compra para conocer cambios de proveedor y referencia de margen.", "Módulo Compras.", List.of("Registrar compra con costo unitario real.", "Guardar entrada.", "Revisar producto y movimientos asociados."), "El sistema conserva el costo operativo de reposición.")
                )),
                new UseCaseModule("Inventario", "Ajustes, mermas, daños y trazabilidad de stock.", List.of(
                        new UseCaseItem("CU-INV-01", "Movimientos", "Ajustar stock por conteo físico", "Corregir stock cuando el conteo real no coincide con el sistema.", "Módulo Movimientos.", List.of("Entrar a Movimientos.", "Seleccionar producto.", "Elegir tipo Corrección.", "Ingresar cantidad corregida.", "Explicar motivo y registrar."), "El stock queda corregido con motivo auditable."),
                        new UseCaseItem("CU-INV-02", "Movimientos", "Registrar merma o daño", "Descontar unidades por daño, vencimiento, pérdida o retiro.", "Módulo Movimientos.", List.of("Seleccionar producto.", "Elegir tipo Merma o Retiro.", "Ingresar cantidad.", "Registrar responsable y motivo."), "La pérdida queda separada de una venta interna normal."),
                        new UseCaseItem("CU-INV-03", "Movimientos", "Consultar historial de stock", "Revisar entradas, salidas y ajustes para explicar diferencias.", "Módulo Movimientos.", List.of("Entrar a Movimientos.", "Buscar producto o tipo de movimiento.", "Revisar fecha, stock anterior y stock nuevo."), "El dueño identifica por qué cambió el inventario.")
                )),
                new UseCaseModule("Salidas", "Ventas internas y descargos no tributarios.", List.of(
                        new UseCaseItem("CU-SAL-01", "Salidas", "Registrar venta interna", "Descontar stock y calcular total como control interno sin reemplazar factura ni comprobante tributario.", "Módulo Salidas.", List.of("Entrar a Salidas.", "Seleccionar producto.", "Ingresar cantidad y precio unitario.", "Marcar confirmación de control interno.", "Registrar salida."), "El stock baja y queda movimiento SALIDA_VENTA_INTERNA."),
                        new UseCaseItem("CU-SAL-02", "Salidas", "Evitar stock negativo", "Bloquear descargas que dejen producto por debajo de cero.", "Módulo Salidas.", List.of("Seleccionar producto.", "Ingresar cantidad mayor al stock disponible.", "Intentar registrar salida.", "Revisar advertencia."), "El sistema protege inventario frente a errores de digitación.")
                )),
                new UseCaseModule("Caja", "Apertura, ingresos, egresos y cierre operativo.", List.of(
                        new UseCaseItem("CU-CAJ-01", "Caja", "Abrir caja diaria", "Registrar el saldo inicial con el que inicia la jornada.", "Módulo Caja.", List.of("Entrar a Caja.", "Ingresar monto inicial.", "Presionar Abrir caja de hoy."), "La caja queda abierta para registrar ingresos y egresos."),
                        new UseCaseItem("CU-CAJ-02", "Caja", "Registrar movimiento de caja", "Anotar ingreso o egreso operativo durante el día.", "Módulo Caja.", List.of("Seleccionar tipo INGRESO o EGRESO.", "Ingresar monto.", "Escribir descripción obligatoria.", "Registrar movimiento."), "La caja conserva un registro simple del flujo diario."),
                        new UseCaseItem("CU-CAJ-03", "Caja", "Cerrar caja", "Comparar saldo esperado contra saldo contado al final del día.", "Módulo Caja.", List.of("Revisar movimientos del día.", "Ingresar saldo contado.", "Presionar Cerrar caja.", "Crear respaldo después del cierre."), "El negocio deja constancia del cierre operativo.")
                )),
                new UseCaseModule("Fiado", "Clientes, cuentas por cobrar y abonos simples.", List.of(
                        new UseCaseItem("CU-FIA-01", "Fiado", "Registrar cliente de fiado", "Guardar un cliente habitual para controlar deuda simple.", "Módulo Fiado.", List.of("Entrar a Fiado.", "Completar nombre del cliente.", "Agregar teléfono, dirección y límite si aplica.", "Guardar cliente."), "El cliente queda disponible para abrir cuentas y registrar abonos."),
                        new UseCaseItem("CU-FIA-02", "Fiado", "Abrir cuenta por cobrar", "Registrar deuda pendiente asociada a un cliente.", "Módulo Fiado.", List.of("Seleccionar cliente.", "Ingresar descripción o monto de nueva cuenta.", "Presionar Abrir cuenta."), "La deuda queda visible en cuentas abiertas del cliente."),
                        new UseCaseItem("CU-FIA-03", "Fiado", "Registrar abono", "Reducir saldo pendiente cuando el cliente paga parte de la deuda.", "Módulo Fiado.", List.of("Seleccionar cliente.", "Seleccionar cuenta abierta.", "Ingresar monto de abono.", "Registrar abono."), "El saldo pendiente disminuye y queda historial del pago.")
                )),
                new UseCaseModule("Reportes", "Productos por comprar, bajo stock, vencimientos y exportación.", List.of(
                        new UseCaseItem("CU-REP-01", "Reportes", "Generar productos por comprar", "Listar productos cuyo stock está por debajo del objetivo.", "Módulo Reportes.", List.of("Entrar a Reportes.", "Seleccionar Productos por comprar.", "Presionar Generar.", "Exportar CSV o PDF si se necesita."), "El dueño obtiene una lista de reposición para comprar mejor."),
                        new UseCaseItem("CU-REP-02", "Reportes", "Exportar CSV editable", "Obtener un archivo modificable para imprimir, revisar o enviar.", "Módulo Reportes.", List.of("Generar reporte.", "Presionar Exportar CSV.", "Abrir archivo desde carpeta de reportes."), "La información queda fuera de la app sin bloquear datos del cliente."),
                        new UseCaseItem("CU-REP-03", "Reportes", "Revisar vencimientos", "Detectar lotes próximos a vencer para priorizar venta o retiro.", "Módulo Reportes.", List.of("Seleccionar reporte de vencimientos.", "Generar reporte.", "Revisar productos próximos a vencer."), "La tienda reduce pérdidas por caducidad.")
                )),
                new UseCaseModule("Respaldos", "Backup, restauración y carpeta local.", List.of(
                        new UseCaseItem("CU-RES-01", "Respaldos", "Crear respaldo manual", "Guardar una copia de la base SQLite antes de cambios sensibles o cierre diario.", "Módulo Respaldos.", List.of("Entrar a Respaldos.", "Escribir observación opcional.", "Presionar Crear respaldo.", "Confirmar que aparezca en la lista."), "Existe una copia recuperable de la base local."),
                        new UseCaseItem("CU-RES-02", "Respaldos", "Abrir carpeta de respaldos", "Ubicar físicamente los archivos para copiarlos a pendrive o nube.", "Módulo Respaldos.", List.of("Entrar a Respaldos.", "Presionar Abrir carpeta.", "Copiar archivo a lugar externo."), "El cliente conserva control sobre sus datos."),
                        new UseCaseItem("CU-RES-03", "Respaldos", "Restaurar respaldo seleccionado", "Volver a un estado anterior de la base con respaldo previo.", "Módulo Respaldos.", List.of("Seleccionar respaldo.", "Presionar Restaurar seleccionado.", "Aceptar respaldo preventivo.", "Cerrar y abrir la app después de restaurar."), "La base vuelve a un estado anterior sin borrar la capacidad de recuperación.")
                )),
                new UseCaseModule("Licencia", "Activación local y modo limitado ético.", List.of(
                        new UseCaseItem("CU-LIC-01", "Licencia", "Activar o renovar licencia", "Registrar código y fecha de vencimiento para habilitar escritura.", "Módulo Licencia.", List.of("Entrar a Licencia.", "Ingresar código entregado al cliente.", "Seleccionar fecha de vencimiento.", "Presionar Activar / renovar."), "El sistema habilita operación según el periodo acordado."),
                        new UseCaseItem("CU-LIC-02", "Licencia", "Trabajar en modo limitado", "Permitir consulta, exportación, reportes y respaldos sin secuestrar información del cliente.", "Módulo Licencia y WriteAccessGuard.", List.of("Dejar vencer o no configurar licencia.", "Entrar a reportes o respaldos.", "Confirmar que lectura sigue disponible.", "Intentar registrar datos para validar bloqueo."), "El cliente conserva acceso a sus datos aunque la escritura esté limitada.")
                ))
        );
    }

    public record UseCaseModule(String name, String description, List<UseCaseItem> cases) {
    }

    public record UseCaseItem(String code, String module, String title, String purpose, String start, List<String> steps, String result) {
    }
}
