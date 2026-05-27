package com.marcosmoreira.mitiendadebarrio.admin.core.application.auditoria;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.auditoria.AuditoriaEvento;

import java.util.List;

/** Puerto de persistencia para bitácora local de acciones relevantes. */
public interface AuditoriaRepository {
    AuditoriaEvento registrar(AuditoriaEvento evento);
    List<AuditoriaEvento> listarRecientes(int limite);
}
