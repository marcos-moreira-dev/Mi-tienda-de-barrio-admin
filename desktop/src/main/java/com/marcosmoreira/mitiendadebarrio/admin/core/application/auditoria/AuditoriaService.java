package com.marcosmoreira.mitiendadebarrio.admin.core.application.auditoria;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.auditoria.AuditoriaEvento;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.auditoria.ResultadoAuditoria;

import java.util.List;

/**
 * Servicio de auditoría local.
 * La auditoría debe ayudar a rastrear acciones humanas sin convertir la app offline en un sistema frágil.
 */
public final class AuditoriaService {
    private final AuditoriaRepository repository;

    public AuditoriaService(AuditoriaRepository repository) {
        this.repository = repository;
    }

    public void registrarExito(Long usuarioId, String modulo, String accion, String entidad, Long entidadId, String resumen) {
        registrarSeguro(AuditoriaEvento.nuevo(usuarioId, modulo, accion, entidad, entidadId, resumen, null, ResultadoAuditoria.OK));
    }

    public void registrarAdvertencia(Long usuarioId, String modulo, String accion, String entidad, Long entidadId, String resumen) {
        registrarSeguro(AuditoriaEvento.nuevo(usuarioId, modulo, accion, entidad, entidadId, resumen, null, ResultadoAuditoria.ADVERTENCIA));
    }

    public void registrarError(Long usuarioId, String modulo, String accion, String entidad, Long entidadId, String resumen, String detalleJson) {
        registrarSeguro(AuditoriaEvento.nuevo(usuarioId, modulo, accion, entidad, entidadId, resumen, detalleJson, ResultadoAuditoria.ERROR));
    }

    public void registrarSistema(String accion, String resumen) {
        registrarSeguro(AuditoriaEvento.nuevo(null, "Sistema", accion, null, null, resumen, null, ResultadoAuditoria.OK));
    }

    public List<AuditoriaEvento> listarRecientes(int limite) {
        return repository.listarRecientes(limite);
    }

    private void registrarSeguro(AuditoriaEvento evento) {
        try {
            repository.registrar(evento);
        } catch (RuntimeException ignored) {
            // No se tumba una operación normal por una falla de bitácora.
            // En operaciones críticas se podrá endurecer esta política en una tanda posterior.
        }
    }
}
