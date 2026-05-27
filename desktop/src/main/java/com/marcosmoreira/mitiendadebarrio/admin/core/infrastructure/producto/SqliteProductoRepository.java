package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.producto;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.producto.ProductoRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.producto.EstadoProducto;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.producto.Producto;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteConnectionFactory;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteRepositorySupport;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.InfrastructureException;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Adaptador SQLite para productos. */
public final class SqliteProductoRepository extends SqliteRepositorySupport implements ProductoRepository {

    public SqliteProductoRepository(SqliteConnectionFactory connectionFactory) { super(connectionFactory); }

    @Override
    public List<Producto> findAll(String query, boolean includeInactive) {
        String sql = baseSelect() + """
                WHERE (? = 1 OR p.estado = 'ACTIVO')
                  AND (? IS NULL OR p.nombre LIKE ? OR p.codigo_interno LIKE ? OR c.nombre LIKE ?)
                ORDER BY p.nombre COLLATE NOCASE
                """;
        String normalizedQuery = query == null || query.isBlank() ? null : "%" + query.strip() + "%";
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, includeInactive ? 1 : 0);
            statement.setString(2, normalizedQuery);
            statement.setString(3, normalizedQuery);
            statement.setString(4, normalizedQuery);
            statement.setString(5, normalizedQuery);
            try (ResultSet rs = statement.executeQuery()) {
                List<Producto> items = new ArrayList<>();
                while (rs.next()) { items.add(map(rs)); }
                return items;
            }
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudieron listar los productos.", ex);
        }
    }

    @Override
    public Optional<Producto> findById(long id) {
        String sql = baseSelect() + " WHERE p.id = ?";
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo leer el producto.", ex);
        }
    }

    @Override
    public Producto save(Producto producto) {
        return producto.id() == null ? insert(producto) : update(producto);
    }

    @Override
    public void updateEstado(long id, EstadoProducto estado) {
        String sql = "UPDATE producto SET estado = ?, updated_at = datetime('now') WHERE id = ?";
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, estado.dbValue());
            statement.setLong(2, id);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo cambiar el estado del producto.", ex);
        }
    }

    private Producto insert(Producto producto) {
        String sql = """
                INSERT INTO producto (codigo_interno, nombre, descripcion, categoria_id, marca_id, unidad_medida_id,
                    proveedor_principal_id, presentacion, precio_compra_referencia, precio_venta,
                    stock_actual, stock_minimo, stock_objetivo, maneja_lote, maneja_vencimiento,
                    perecible, refrigerado, ruta_foto, estado, observacion, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now'))
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(statement, producto, false);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) { return findById(keys.getLong(1)).orElseThrow(); }
                throw new InfrastructureException("No se pudo obtener el ID del producto creado.");
            }
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo crear el producto.", ex);
        }
    }

    private Producto update(Producto producto) {
        String sql = """
                UPDATE producto
                SET codigo_interno = ?, nombre = ?, descripcion = ?, categoria_id = ?, marca_id = ?, unidad_medida_id = ?,
                    proveedor_principal_id = ?, presentacion = ?, precio_compra_referencia = ?, precio_venta = ?,
                    stock_actual = ?, stock_minimo = ?, stock_objetivo = ?, maneja_lote = ?, maneja_vencimiento = ?,
                    perecible = ?, refrigerado = ?, ruta_foto = ?, estado = ?, observacion = ?, updated_at = datetime('now')
                WHERE id = ?
                """;
        try (Connection connection = connectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, producto, true);
            statement.executeUpdate();
            return findById(producto.id()).orElseThrow();
        } catch (SQLException ex) {
            throw new InfrastructureException("No se pudo actualizar el producto.", ex);
        }
    }

    private String baseSelect() {
        return """
                SELECT p.id, p.codigo_interno, p.nombre, p.descripcion, p.categoria_id, c.nombre AS categoria_nombre,
                       p.marca_id, m.nombre AS marca_nombre, p.unidad_medida_id, u.nombre AS unidad_medida_nombre,
                       p.proveedor_principal_id, pr.nombre AS proveedor_principal_nombre, p.presentacion,
                       p.precio_compra_referencia, p.precio_venta, p.stock_actual, p.stock_minimo, p.stock_objetivo,
                       p.maneja_lote, p.maneja_vencimiento, p.perecible, p.refrigerado, p.ruta_foto, p.estado, p.observacion
                FROM producto p
                JOIN categoria c ON c.id = p.categoria_id
                JOIN unidad_medida u ON u.id = p.unidad_medida_id
                LEFT JOIN marca m ON m.id = p.marca_id
                LEFT JOIN proveedor pr ON pr.id = p.proveedor_principal_id
                """;
    }

    private void bind(PreparedStatement statement, Producto producto, boolean withId) throws SQLException {
        statement.setString(1, blankToNull(producto.codigoInterno()));
        statement.setString(2, normalize(producto.nombre()));
        statement.setString(3, blankToNull(producto.descripcion()));
        statement.setLong(4, producto.categoriaId());
        setNullableLong(statement, 5, producto.marcaId());
        statement.setLong(6, producto.unidadMedidaId());
        setNullableLong(statement, 7, producto.proveedorPrincipalId());
        statement.setString(8, blankToNull(producto.presentacion()));
        statement.setBigDecimal(9, zeroIfNull(producto.precioCompraReferencia()));
        statement.setBigDecimal(10, zeroIfNull(producto.precioVenta()));
        statement.setBigDecimal(11, zeroIfNull(producto.stockActual()));
        statement.setBigDecimal(12, zeroIfNull(producto.stockMinimo()));
        if (producto.stockObjetivo() == null) { statement.setNull(13, Types.NUMERIC); } else { statement.setBigDecimal(13, producto.stockObjetivo()); }
        statement.setInt(14, producto.manejaLote() ? 1 : 0);
        statement.setInt(15, producto.manejaVencimiento() ? 1 : 0);
        statement.setInt(16, producto.perecible() ? 1 : 0);
        statement.setInt(17, producto.refrigerado() ? 1 : 0);
        statement.setString(18, blankToNull(producto.rutaFoto()));
        statement.setString(19, producto.estado().dbValue());
        statement.setString(20, blankToNull(producto.observacion()));
        if (withId) { statement.setLong(21, producto.id()); }
    }

    private Producto map(ResultSet rs) throws SQLException {
        return new Producto(
                rs.getLong("id"), emptyIfNull(rs.getString("codigo_interno")), rs.getString("nombre"), emptyIfNull(rs.getString("descripcion")),
                rs.getLong("categoria_id"), rs.getString("categoria_nombre"), nullableLong(rs, "marca_id"), emptyIfNull(rs.getString("marca_nombre")),
                rs.getLong("unidad_medida_id"), rs.getString("unidad_medida_nombre"), nullableLong(rs, "proveedor_principal_id"),
                emptyIfNull(rs.getString("proveedor_principal_nombre")), emptyIfNull(rs.getString("presentacion")),
                bigDecimalOrZero(rs, "precio_compra_referencia"), bigDecimalOrZero(rs, "precio_venta"), bigDecimalOrZero(rs, "stock_actual"),
                bigDecimalOrZero(rs, "stock_minimo"), rs.getBigDecimal("stock_objetivo"), rs.getInt("maneja_lote") == 1,
                rs.getInt("maneja_vencimiento") == 1, rs.getInt("perecible") == 1, rs.getInt("refrigerado") == 1,
                emptyIfNull(rs.getString("ruta_foto")), EstadoProducto.fromDb(rs.getString("estado")), emptyIfNull(rs.getString("observacion"))
        );
    }
}
