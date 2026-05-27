package com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.opcional;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.opcional.OpcionalesMinimosRepository;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.opcional.*;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteConnectionFactory;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.SqliteRepositorySupport;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.InfrastructureException;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/** Adaptador SQLite para opcionales mínimos del ERP local. */
public final class SqliteOpcionalesMinimosRepository extends SqliteRepositorySupport implements OpcionalesMinimosRepository {
    public SqliteOpcionalesMinimosRepository(SqliteConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public List<TipoActivoNegocio> listarTiposActivoActivos() {
        String sql = "SELECT id,codigo,nombre,descripcion,estado FROM tipo_activo_negocio WHERE estado='ACTIVO' ORDER BY nombre";
        List<TipoActivoNegocio> out = new ArrayList<>();
        try (Connection c = connectionFactory.openConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(new TipoActivoNegocio(rs.getLong("id"), rs.getString("codigo"), rs.getString("nombre"), rs.getString("descripcion"), rs.getString("estado")));
            return out;
        } catch (SQLException ex) { throw new InfrastructureException("No se pudieron listar tipos de activo.", ex); }
    }

    @Override
    public ActivoNegocio registrarActivo(RegistroActivoNegocio r) {
        String sql = "INSERT INTO activo_negocio(tipo_activo_id,codigo,nombre,descripcion,fecha_adquisicion,valor_estimado,ubicacion,responsable,observacion) VALUES(?,?,?,?,?,?,?,?,?)";
        try (Connection c = connectionFactory.openConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, r.tipoActivoId());
            ps.setString(2, blankToNull(r.codigo()));
            ps.setString(3, normalize(r.nombre()));
            ps.setString(4, blankToNull(r.descripcion()));
            ps.setString(5, blankToNull(r.fechaAdquisicion()));
            ps.setBigDecimal(6, r.valorEstimado() == null ? BigDecimal.ZERO : r.valorEstimado());
            ps.setString(7, blankToNull(r.ubicacion()));
            ps.setString(8, blankToNull(r.responsable()));
            ps.setString(9, blankToNull(r.observacion()));
            ps.executeUpdate();
            long id = generatedId(ps);
            return new ActivoNegocio(id, r.tipoActivoId(), r.codigo(), normalize(r.nombre()), r.descripcion(), r.fechaAdquisicion(), r.valorEstimado() == null ? BigDecimal.ZERO : r.valorEstimado(), "ACTIVO", r.ubicacion(), r.responsable(), r.observacion());
        } catch (SQLException ex) { throw new InfrastructureException("No se pudo registrar el activo.", ex); }
    }

    @Override
    public List<ActivoNegocio> listarActivosRecientes(int limite) {
        String sql = "SELECT id,tipo_activo_id,codigo,nombre,descripcion,fecha_adquisicion,valor_estimado,estado,ubicacion,responsable,observacion FROM activo_negocio ORDER BY id DESC LIMIT ?";
        List<ActivoNegocio> out = new ArrayList<>();
        try (Connection c = connectionFactory.openConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapActivo(rs));
            }
            return out;
        } catch (SQLException ex) { throw new InfrastructureException("No se pudieron listar activos.", ex); }
    }

    @Override
    public List<CargoEmpleado> listarCargosActivos() {
        String sql = "SELECT id,codigo,nombre,descripcion,estado FROM cargo_empleado WHERE estado='ACTIVO' ORDER BY nombre";
        List<CargoEmpleado> out = new ArrayList<>();
        try (Connection c = connectionFactory.openConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(new CargoEmpleado(rs.getLong("id"), rs.getString("codigo"), rs.getString("nombre"), rs.getString("descripcion"), rs.getString("estado")));
            return out;
        } catch (SQLException ex) { throw new InfrastructureException("No se pudieron listar cargos.", ex); }
    }

    @Override
    public EmpleadoLocal registrarEmpleado(RegistroEmpleadoLocal r) {
        String sql = "INSERT INTO empleado_local(cargo_id,tercero_id,nombre,identificacion,telefono,fecha_ingreso,observacion) VALUES(?,?,?,?,?,?,?)";
        try (Connection c = connectionFactory.openConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setNullableLong(ps, 1, r.cargoId());
            setNullableLong(ps, 2, r.terceroId());
            ps.setString(3, normalize(r.nombre()));
            ps.setString(4, blankToNull(r.identificacion()));
            ps.setString(5, blankToNull(r.telefono()));
            ps.setString(6, blankToNull(r.fechaIngreso()));
            ps.setString(7, blankToNull(r.observacion()));
            ps.executeUpdate();
            long id = generatedId(ps);
            return new EmpleadoLocal(id, r.cargoId(), r.terceroId(), normalize(r.nombre()), r.identificacion(), r.telefono(), r.fechaIngreso(), "ACTIVO", r.observacion());
        } catch (SQLException ex) { throw new InfrastructureException("No se pudo registrar el empleado.", ex); }
    }

    @Override
    public List<EmpleadoLocal> listarEmpleadosActivos() {
        String sql = "SELECT id,cargo_id,tercero_id,nombre,identificacion,telefono,fecha_ingreso,estado,observacion FROM empleado_local WHERE estado='ACTIVO' ORDER BY nombre";
        List<EmpleadoLocal> out = new ArrayList<>();
        try (Connection c = connectionFactory.openConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(mapEmpleado(rs));
            return out;
        } catch (SQLException ex) { throw new InfrastructureException("No se pudieron listar empleados.", ex); }
    }

    @Override
    public List<IndicadorOperativo> listarIndicadoresActivos() {
        String sql = "SELECT id,codigo,nombre,descripcion,modulo,orden_visual,activo FROM indicador_operativo WHERE activo=1 ORDER BY modulo, orden_visual, nombre";
        List<IndicadorOperativo> out = new ArrayList<>();
        try (Connection c = connectionFactory.openConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(new IndicadorOperativo(rs.getLong("id"), rs.getString("codigo"), rs.getString("nombre"), rs.getString("descripcion"), rs.getString("modulo"), rs.getInt("orden_visual"), rs.getInt("activo") == 1));
            return out;
        } catch (SQLException ex) { throw new InfrastructureException("No se pudieron listar indicadores.", ex); }
    }

    @Override
    public List<PlantillaImportacion> listarPlantillasImportacionActivas() {
        String sql = "SELECT id,codigo,nombre,tipo_importacion,encabezados_csv,descripcion,activo FROM plantilla_importacion WHERE activo=1 ORDER BY tipo_importacion,nombre";
        List<PlantillaImportacion> out = new ArrayList<>();
        try (Connection c = connectionFactory.openConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(new PlantillaImportacion(rs.getLong("id"), rs.getString("codigo"), rs.getString("nombre"), rs.getString("tipo_importacion"), rs.getString("encabezados_csv"), rs.getString("descripcion"), rs.getInt("activo") == 1));
            return out;
        } catch (SQLException ex) { throw new InfrastructureException("No se pudieron listar plantillas de importación.", ex); }
    }

    @Override
    public LoteImportacion registrarLoteImportacion(RegistroLoteImportacion r) {
        String sql = "INSERT INTO lote_importacion(plantilla_id,tipo_importacion,archivo_origen,total_filas,filas_validas,filas_con_error,checksum_archivo,observacion,estado) VALUES(?,?,?,?,?,?,?,?,?)";
        try (Connection c = connectionFactory.openConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setNullableLong(ps, 1, r.plantillaId());
            ps.setString(2, normalize(r.tipoImportacion()).toUpperCase(Locale.ROOT));
            ps.setString(3, normalize(r.archivoOrigen()));
            ps.setInt(4, Math.max(0, r.totalFilas()));
            ps.setInt(5, Math.max(0, r.filasValidas()));
            ps.setInt(6, Math.max(0, r.filasConError()));
            ps.setString(7, blankToNull(r.checksumArchivo()));
            ps.setString(8, blankToNull(r.observacion()));
            ps.setString(9, r.filasConError() > 0 ? "VALIDADO" : "PROCESADO");
            ps.executeUpdate();
            long id = generatedId(ps);
            return new LoteImportacion(id, r.plantillaId(), normalize(r.tipoImportacion()).toUpperCase(Locale.ROOT), normalize(r.archivoOrigen()), null, r.filasConError() > 0 ? "VALIDADO" : "PROCESADO", Math.max(0, r.totalFilas()), Math.max(0, r.filasValidas()), Math.max(0, r.filasConError()), r.checksumArchivo(), r.observacion());
        } catch (SQLException ex) { throw new InfrastructureException("No se pudo registrar el lote de importación.", ex); }
    }

    @Override
    public void registrarErrorImportacion(long loteImportacionId, int numeroFila, String campo, String valorOriginal, String mensaje, String severidad) {
        String sql = "INSERT INTO error_importacion(lote_importacion_id,numero_fila,campo,valor_original,mensaje,severidad) VALUES(?,?,?,?,?,?)";
        try (Connection c = connectionFactory.openConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, loteImportacionId);
            ps.setInt(2, numeroFila);
            ps.setString(3, blankToNull(campo));
            ps.setString(4, blankToNull(valorOriginal));
            ps.setString(5, normalize(mensaje));
            ps.setString(6, normalize(severidad).isBlank() ? "ERROR" : normalize(severidad).toUpperCase(Locale.ROOT));
            ps.executeUpdate();
        } catch (SQLException ex) { throw new InfrastructureException("No se pudo registrar el error de importación.", ex); }
    }

    @Override
    public List<ChecklistOperativo> listarChecklistsActivos() {
        String sql = "SELECT id,codigo,nombre,descripcion,frecuencia,activo FROM checklist_operativo WHERE activo=1 ORDER BY frecuencia,nombre";
        Map<Long, ChecklistOperativo> checklists = new LinkedHashMap<>();
        try (Connection c = connectionFactory.openConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                long id = rs.getLong("id");
                checklists.put(id, new ChecklistOperativo(id, rs.getString("codigo"), rs.getString("nombre"), rs.getString("descripcion"), rs.getString("frecuencia"), rs.getInt("activo") == 1, new ArrayList<>()));
            }
            cargarItems(c, checklists);
            return new ArrayList<>(checklists.values());
        } catch (SQLException ex) { throw new InfrastructureException("No se pudieron listar checklists.", ex); }
    }

    private void cargarItems(Connection c, Map<Long, ChecklistOperativo> checklists) throws SQLException {
        if (checklists.isEmpty()) return;
        String sql = "SELECT id,checklist_id,orden,titulo,descripcion,modulo_relacionado,activo FROM checklist_item WHERE activo=1 ORDER BY checklist_id, orden, id";
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                long checklistId = rs.getLong("checklist_id");
                ChecklistOperativo checklist = checklists.get(checklistId);
                if (checklist != null) {
                    checklist.items().add(new ChecklistItem(rs.getLong("id"), checklistId, rs.getInt("orden"), rs.getString("titulo"), rs.getString("descripcion"), rs.getString("modulo_relacionado"), rs.getInt("activo") == 1));
                }
            }
        }
    }

    private ActivoNegocio mapActivo(ResultSet rs) throws SQLException {
        return new ActivoNegocio(rs.getLong("id"), rs.getLong("tipo_activo_id"), rs.getString("codigo"), rs.getString("nombre"), rs.getString("descripcion"), rs.getString("fecha_adquisicion"), bigDecimalOrZero(rs, "valor_estimado"), rs.getString("estado"), rs.getString("ubicacion"), rs.getString("responsable"), rs.getString("observacion"));
    }

    private EmpleadoLocal mapEmpleado(ResultSet rs) throws SQLException {
        return new EmpleadoLocal(rs.getLong("id"), nullableLong(rs, "cargo_id"), nullableLong(rs, "tercero_id"), rs.getString("nombre"), rs.getString("identificacion"), rs.getString("telefono"), rs.getString("fecha_ingreso"), rs.getString("estado"), rs.getString("observacion"));
    }

    private long generatedId(PreparedStatement ps) throws SQLException {
        try (ResultSet keys = ps.getGeneratedKeys()) {
            if (keys.next()) return keys.getLong(1);
        }
        throw new SQLException("No se pudo leer el id generado.");
    }
}
