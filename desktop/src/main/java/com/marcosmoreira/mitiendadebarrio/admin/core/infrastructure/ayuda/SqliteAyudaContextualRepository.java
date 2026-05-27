package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.ayuda;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.ayuda.AyudaContextualRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.ayuda.AyudaContextual;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteConnectionFactory;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteRepositorySupport;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.InfrastructureException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Adaptador SQLite para el mini manual y ayuda contextual local. */
public final class SqliteAyudaContextualRepository extends SqliteRepositorySupport implements AyudaContextualRepository {


    public SqliteAyudaContextualRepository(SqliteConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    public List<AyudaContextual> findAllVisible() {
        return query("""
                SELECT *
                FROM ayuda_contextual
                WHERE estado = 'ACTIVA'
                ORDER BY modulo COLLATE NOCASE, id
                """, null);
    }

    public List<AyudaContextual> findByModulo(String modulo) {
        return query("""
                SELECT *
                FROM ayuda_contextual
                WHERE estado = 'ACTIVA' AND modulo = ?
                ORDER BY id
                """, modulo);
    }

    public void ensureDefaultContent() {
        try (Connection connection = connectionFactory.openConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM ayuda_contextual WHERE estado = 'ACTIVA'")) {
            if (rs.next() && rs.getInt(1) > 0) return;
            insertarBase(connection);
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo preparar la ayuda contextual.", ex);
        }
    }

    private void insertarBase(Connection connection) throws SQLException {
        String sql = """
                INSERT OR IGNORE INTO ayuda_contextual(modulo, clave, titulo, contenido, estado, updated_at)
                VALUES(?, ?, ?, ?, 'ACTIVA', datetime('now'))
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            insert(statement, "inicio", "uso-diario", "Uso diario recomendado", "Revise bajo stock, registre compras, registre salidas internas y cree respaldo al final del día.");
            insert(statement, "productos", "productos", "Productos", "Mantenga nombre, categoría, unidad, stock mínimo y stock objetivo. Use vencimiento solo cuando aplique.");
            insert(statement, "compras", "compras", "Compras", "Cada entrada debe registrar proveedor, costo y cantidad para mantener stock e historial.");
            insert(statement, "salidas", "ventas-internas", "Ventas internas", "Las salidas internas descuentan stock, pero no reemplazan facturación ni comprobantes oficiales.");
            insert(statement, "reportes", "reportes", "Reportes", "Use productos por comprar para planificar reposición con stock mínimo y objetivo.");
            insert(statement, "respaldos", "respaldos", "Respaldos", "Cree respaldos frecuentes y guárdelos fuera de la computadora cuando sea posible.");
            insert(statement, "licencia", "licencia", "Licencia", "Si la licencia vence, el sistema puede entrar en modo limitado sin borrar ni secuestrar datos.");
            insert(statement, "caja", "caja-diaria", "Caja diaria", "Caja es opcional y operativa. No reemplaza contabilidad formal.");
            insert(statement, "fiado", "fiado", "Fiado", "Use fiado solo si el negocio realmente lo maneja. Registre abonos para evitar confusiones.");
            insert(statement, "inicio", "ruta-diaria-operativa", "Ruta diaria recomendada", "1) Abrir o revisar caja. 2) Registrar ventas internas y fiado cuando aplique. 3) Registrar compras o gastos del día. 4) Revisar productos por comprar. 5) Cerrar caja y crear respaldo.");
            insert(statement, "inicio", "ruta-semanal-control", "Ruta semanal de control", "Revise productos próximos a vencer, productos agotados, fiado pendiente, cuentas por pagar, gastos de la semana y último respaldo.");
            insert(statement, "ventas-internas", "venta-pagada-vs-fiada", "Venta pagada y venta fiada", "Una venta pagada debe conectarse con caja. Una venta fiada crea una cuenta por cobrar y no cuenta como dinero recibido hasta registrar un abono.");
            insert(statement, "compras", "compra-pagada-vs-credito", "Compra pagada y compra a crédito", "Una compra pagada representa salida de dinero. Una compra a crédito aumenta inventario y crea una cuenta por pagar.");
            insert(statement, "caja", "cierre-caja", "Cierre de caja", "Compare saldo esperado y saldo contado. Si hay diferencia, deje observación. No fuerce números sin explicación.");
            insert(statement, "fiado", "abonos", "Abonos de fiado", "El abono baja la cuenta por cobrar y entra a caja si se recibió dinero real.");
            insert(statement, "inventario", "conteo-ajuste", "Conteo y ajuste de inventario", "Use conteo para comparar stock físico contra el sistema. Use ajuste solo con motivo real.");
            insert(statement, "reportes", "lectura-reportes", "Cómo leer reportes", "Use productos por comprar para reponer, fiado pendiente para cobrar y cierre de caja para revisar el día.");
            insert(statement, "respaldos", "respaldo-seguro", "Respaldo seguro", "Cree respaldos frecuentes y guarde una copia fuera de la computadora principal.");
            insert(statement, "fiscalidad-preparada", "documento-preparado", "Documento preparado", "Un documento preparado ayuda a ordenar información interna, pero no reemplaza comprobante autorizado por el SRI.");
            insert(statement, "contabilidad-basica", "asiento-manual", "Asiento manual", "El sistema exige que debe y haber cuadren, pero eso no reemplaza revisión profesional.");
            insert(statement, "opciones-minimas", "importacion-segura", "Importación segura", "Antes de importar CSV, revise encabezados y errores. No inserte datos definitivos si el archivo tiene valores inválidos.");
        }
    }

    private void insert(PreparedStatement statement, String modulo, String clave, String titulo, String contenido) throws SQLException {
        statement.setString(1, modulo);
        statement.setString(2, clave);
        statement.setString(3, titulo);
        statement.setString(4, contenido);
        statement.executeUpdate();
    }

    private List<AyudaContextual> query(String sql, String modulo) {
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (modulo != null) statement.setString(1, modulo);
            try (ResultSet rs = statement.executeQuery()) {
                List<AyudaContextual> out = new ArrayList<>();
                int orden = 1;
                while (rs.next()) {
                    out.add(new AyudaContextual(
                            rs.getLong("id"),
                            rs.getString("modulo"),
                            rs.getString("titulo"),
                            rs.getString("contenido"),
                            orden++,
                            true
                    ));
                }
                return out;
            }
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo leer la ayuda contextual.", ex);
        }
    }
}
