package com.marcosmoreira.mitiendadebarrio.admin.core.application.seguridad;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.auditoria.AuditoriaService;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.seguridad.EstadoUsuarioLocal;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.seguridad.SesionUsuarioLocal;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.seguridad.UsuarioLocalCredenciales;
import com.marcosmoreira.mitiendadebarrio.admin.shared.result.OperationResult;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

/** Servicio de ingreso local. No es seguridad bancaria; es control operativo para la app offline. */
public final class UsuarioLocalService {
    private static final String SUPPORTED_ALGORITHM = "SHA-256";

    private final UsuarioLocalRepository repository;
    private final AuditoriaService auditoriaService;

    public UsuarioLocalService(UsuarioLocalRepository repository) {
        this(repository, null);
    }

    public UsuarioLocalService(UsuarioLocalRepository repository, AuditoriaService auditoriaService) {
        this.repository = repository;
        this.auditoriaService = auditoriaService;
    }

    public OperationResult<SesionUsuarioLocal> autenticar(String nombreUsuario, String password) {
        String usuarioNormalizado = normalize(nombreUsuario);
        if (usuarioNormalizado.isBlank() || password == null || password.isBlank()) {
            return OperationResult.failure("Ingrese usuario y contraseña.");
        }

        return repository.buscarCredencialesPorUsuario(usuarioNormalizado)
                .map(credenciales -> autenticarConCredenciales(credenciales, password))
                .orElseGet(() -> {
                    auditarAdvertencia(null, "LOGIN_FALLIDO", "usuario_local", null, "Intento de acceso con usuario no registrado: " + usuarioNormalizado);
                    return OperationResult.failure("Credenciales incorrectas.");
                });
    }

    private OperationResult<SesionUsuarioLocal> autenticarConCredenciales(UsuarioLocalCredenciales credenciales, String password) {
        if (credenciales.estado() != EstadoUsuarioLocal.ACTIVO) {
            auditarAdvertencia(credenciales.id(), "LOGIN_BLOQUEADO", "usuario_local", credenciales.id(), "Intento de acceso con usuario local no activo: " + credenciales.nombreUsuario());
            return OperationResult.failure("El usuario local no está activo. Revise la configuración de usuarios.");
        }
        if (!SUPPORTED_ALGORITHM.equalsIgnoreCase(credenciales.algoritmoHash())) {
            auditarError(credenciales.id(), "LOGIN_ERROR_ALGORITMO", "usuario_local", credenciales.id(), "El usuario local usa un algoritmo de contraseña no soportado: " + credenciales.nombreUsuario());
            return OperationResult.failure("El usuario local usa un algoritmo de contraseña no soportado por esta versión.");
        }

        String calculatedHash = hashPassword(password, credenciales.passwordSalt());
        if (!constantTimeEquals(calculatedHash, credenciales.passwordHash())) {
            auditarAdvertencia(credenciales.id(), "LOGIN_FALLIDO", "usuario_local", credenciales.id(), "Intento de acceso con contraseña incorrecta: " + credenciales.nombreUsuario());
            return OperationResult.failure("Credenciales incorrectas.");
        }

        repository.actualizarUltimoAcceso(credenciales.id());
        auditarExito(credenciales.id(), "LOGIN_CORRECTO", "usuario_local", credenciales.id(), "Acceso local correcto: " + credenciales.nombreUsuario());
        List<String> roles = repository.rolesDeUsuario(credenciales.id());
        SesionUsuarioLocal sesion = new SesionUsuarioLocal(
                credenciales.id(),
                credenciales.nombreUsuario(),
                credenciales.nombreVisible(),
                List.copyOf(roles),
                credenciales.debeCambiarClave()
        );
        return OperationResult.success(sesion, "Acceso correcto.");
    }

    public String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SUPPORTED_ALGORITHM);
            byte[] bytes = digest.digest((salt + ":" + password).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("No se encontró SHA-256 en la JVM local.", ex);
        }
    }

    private boolean constantTimeEquals(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        byte[] a = left.getBytes(StandardCharsets.UTF_8);
        byte[] b = right.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(a, b);
    }


    private void auditarExito(Long usuarioId, String accion, String entidad, Long entidadId, String resumen) {
        if (auditoriaService != null) {
            auditoriaService.registrarExito(usuarioId, "Seguridad", accion, entidad, entidadId, resumen);
        }
    }

    private void auditarAdvertencia(Long usuarioId, String accion, String entidad, Long entidadId, String resumen) {
        if (auditoriaService != null) {
            auditoriaService.registrarAdvertencia(usuarioId, "Seguridad", accion, entidad, entidadId, resumen);
        }
    }

    private void auditarError(Long usuarioId, String accion, String entidad, Long entidadId, String resumen) {
        if (auditoriaService != null) {
            auditoriaService.registrarError(usuarioId, "Seguridad", accion, entidad, entidadId, resumen, null);
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.strip();
    }
}
