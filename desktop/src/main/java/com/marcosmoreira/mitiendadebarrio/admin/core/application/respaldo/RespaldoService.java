package com.marcosmoreira.mitiendadebarrio.admin.core.application.respaldo;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.auditoria.AuditoriaService;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.respaldo.RespaldoSistema;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.respaldo.TipoRespaldo;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.InfrastructureException;
import com.marcosmoreira.mitiendadebarrio.admin.shared.result.OperationResult;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

/** Casos de uso de respaldo y restauración local. */
public final class RespaldoService {
    private static final DateTimeFormatter FILE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final byte[] SQLITE_HEADER = new byte[] {
            'S', 'Q', 'L', 'i', 't', 'e', ' ', 'f', 'o', 'r', 'm', 'a', 't', ' ', '3', 0
    };

    private final Path databaseFile;
    private final Path backupsDirectory;
    private final RespaldoRepository repository;
    private final AuditoriaService auditoriaService;

    public RespaldoService(Path databaseFile, Path backupsDirectory, RespaldoRepository repository) {
        this(databaseFile, backupsDirectory, repository, null);
    }

    public RespaldoService(Path databaseFile, Path backupsDirectory, RespaldoRepository repository, AuditoriaService auditoriaService) {
        this.databaseFile = databaseFile;
        this.backupsDirectory = backupsDirectory;
        this.repository = repository;
        this.auditoriaService = auditoriaService;
    }

    public List<RespaldoSistema> listarRecientes() { return repository.listarRecientes(50); }

    public OperationResult<RespaldoSistema> crearManual(String observacion) { return crear(TipoRespaldo.MANUAL, observacion); }

    public OperationResult<Void> restaurar(Path backupFile) {
        if (backupFile == null || !Files.exists(backupFile)) {
            auditarAdvertencia("RESTAURAR_RESPALDO", "Intento de restauración sin seleccionar un respaldo existente.");
            return OperationResult.failure("Seleccione un respaldo existente.");
        }
        try {
            Path backup = backupFile.toAbsolutePath().normalize();
            Path database = databaseFile.toAbsolutePath().normalize();
            if (backup.equals(database) || (Files.exists(database) && Files.isSameFile(backup, database))) {
                auditarAdvertencia("RESTAURAR_RESPALDO", "Se rechazó una restauración usando la base activa como respaldo.");
                return OperationResult.failure("No puede restaurar usando la misma base activa como respaldo.");
            }

            List<String> validationErrors = validarArchivoSqlite(backup);
            if (!validationErrors.isEmpty()) {
                String resumen = "El respaldo no pasó la validación: " + String.join(" | ", validationErrors);
                auditarError("VALIDAR_RESPALDO", resumen, detalleJson(validationErrors));
                return OperationResult.failure("El respaldo seleccionado no es seguro para restaurar: " + String.join("; ", validationErrors));
            }

            OperationResult<RespaldoSistema> preRestore = crear(TipoRespaldo.PRE_RESTAURACION, "Respaldo automático antes de restaurar.");
            if (!preRestore.success()) {
                auditarError("RESTAURAR_RESPALDO", "No se pudo crear respaldo preventivo antes de restaurar.", null);
                return OperationResult.failure("No se pudo crear el respaldo preventivo antes de restaurar.");
            }

            Files.copy(backup, databaseFile, StandardCopyOption.REPLACE_EXISTING);
            repository.marcarRestaurado(backup, "Restaurado manualmente desde la aplicación local.");
            auditarExito("RESTAURAR_RESPALDO", "Base restaurada desde: " + backup.getFileName());
            return OperationResult.success(null, "Base restaurada. Cierre y vuelva a abrir la aplicación para continuar con seguridad.");
        } catch (IOException ex) {
            auditarError("RESTAURAR_RESPALDO", "No se pudo restaurar el respaldo seleccionado.", jsonEscape(ex.getMessage()));
            throw new InfrastructureException("No se pudo restaurar el respaldo seleccionado.", ex);
        }
    }

    public List<String> validarArchivoSqlite(Path archivo) {
        List<String> errores = new ArrayList<>();
        if (archivo == null) {
            errores.add("ruta vacía");
            return errores;
        }
        Path file = archivo.toAbsolutePath().normalize();
        if (!Files.exists(file)) {
            errores.add("el archivo no existe");
            return errores;
        }
        try {
            if (Files.size(file) <= SQLITE_HEADER.length) {
                errores.add("el archivo está vacío o incompleto");
                return errores;
            }
            if (!tieneCabeceraSqlite(file)) {
                errores.add("el archivo no tiene cabecera SQLite válida");
                return errores;
            }
            errores.addAll(validarPragmas(file));
        } catch (IOException ex) {
            errores.add("no se pudo leer el archivo: " + ex.getMessage());
        }
        return errores;
    }

    private OperationResult<RespaldoSistema> crear(TipoRespaldo tipo, String observacion) {
        try {
            Files.createDirectories(backupsDirectory);
            if (!Files.exists(databaseFile)) {
                auditarError("CREAR_RESPALDO", "No existe base local para respaldar.", null);
                return OperationResult.failure("No existe base local para respaldar.");
            }
            List<String> erroresBaseActiva = validarArchivoSqlite(databaseFile);
            if (!erroresBaseActiva.isEmpty()) {
                auditarError("CREAR_RESPALDO", "La base activa no pasó validación antes de respaldar.", detalleJson(erroresBaseActiva));
                return OperationResult.failure("La base local no pasó la validación previa al respaldo: " + String.join("; ", erroresBaseActiva));
            }

            String name = "mi_tienda_barrio_" + tipo.name().toLowerCase() + "_" + FILE_FORMAT.format(LocalDateTime.now()) + ".sqlite";
            Path output = backupsDirectory.resolve(name);
            Files.copy(databaseFile, output, StandardCopyOption.REPLACE_EXISTING);

            List<String> erroresCopia = validarArchivoSqlite(output);
            if (!erroresCopia.isEmpty()) {
                Files.deleteIfExists(output);
                auditarError("CREAR_RESPALDO", "La copia del respaldo no pasó validación.", detalleJson(erroresCopia));
                return OperationResult.failure("La copia del respaldo no pasó validación: " + String.join("; ", erroresCopia));
            }

            long size = Files.size(output);
            String hash = sha256(output);
            RespaldoSistema respaldo = repository.registrar(output, tipo, size, hash, observacion);
            auditarExito("CREAR_RESPALDO", "Respaldo creado: " + output.getFileName());
            return OperationResult.success(respaldo, "Respaldo creado en: " + output.toAbsolutePath());
        } catch (IOException ex) {
            auditarError("CREAR_RESPALDO", "No se pudo crear el respaldo local.", jsonEscape(ex.getMessage()));
            throw new InfrastructureException("No se pudo crear el respaldo local.", ex);
        }
    }

    private boolean tieneCabeceraSqlite(Path file) throws IOException {
        byte[] header = new byte[SQLITE_HEADER.length];
        try (InputStream input = Files.newInputStream(file)) {
            int read = input.read(header);
            if (read != SQLITE_HEADER.length) return false;
        }
        for (int i = 0; i < SQLITE_HEADER.length; i++) {
            if (header[i] != SQLITE_HEADER[i]) return false;
        }
        return true;
    }

    private List<String> validarPragmas(Path file) {
        List<String> errores = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + file.toAbsolutePath());
             Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            try (ResultSet rs = statement.executeQuery("PRAGMA integrity_check")) {
                while (rs.next()) {
                    String value = rs.getString(1);
                    if (!"ok".equalsIgnoreCase(value)) errores.add("integrity_check: " + value);
                }
            }
            try (ResultSet rs = statement.executeQuery("PRAGMA foreign_key_check")) {
                while (rs.next()) {
                    errores.add("foreign_key_check: tabla=" + rs.getString(1) + ", fila=" + rs.getString(2));
                }
            }
        } catch (SQLException ex) {
            errores.add("no se pudo validar SQLite: " + ex.getMessage());
        }
        return errores;
    }

    private String sha256(Path file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream input = Files.newInputStream(file)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) != -1) digest.update(buffer, 0, read);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException | IOException ex) {
            throw new InfrastructureException("No se pudo calcular hash del respaldo.", ex);
        }
    }

    private void auditarExito(String accion, String resumen) {
        if (auditoriaService != null) auditoriaService.registrarExito(null, "Respaldos", accion, "respaldo_sistema", null, resumen);
    }

    private void auditarAdvertencia(String accion, String resumen) {
        if (auditoriaService != null) auditoriaService.registrarAdvertencia(null, "Respaldos", accion, "respaldo_sistema", null, resumen);
    }

    private void auditarError(String accion, String resumen, String detalleJson) {
        if (auditoriaService != null) auditoriaService.registrarError(null, "Respaldos", accion, "respaldo_sistema", null, resumen, detalleJson);
    }

    private String detalleJson(List<String> errores) {
        StringBuilder builder = new StringBuilder("{\"errores\":[");
        for (int i = 0; i < errores.size(); i++) {
            if (i > 0) builder.append(',');
            builder.append('"').append(jsonEscape(errores.get(i))).append('"');
        }
        return builder.append("]}").toString();
    }

    private String jsonEscape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
