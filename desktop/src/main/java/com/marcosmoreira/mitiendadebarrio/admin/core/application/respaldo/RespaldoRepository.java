package com.marcosmoreira.mitiendadebarrio.admin.core.application.respaldo;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.respaldo.RespaldoSistema;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.respaldo.TipoRespaldo;

import java.nio.file.Path;
import java.util.List;

public interface RespaldoRepository {
    RespaldoSistema registrar(Path archivo, TipoRespaldo tipo, long pesoBytes, String hashSha256, String observacion);
    void marcarRestaurado(Path archivo, String observacion);
    List<RespaldoSistema> listarRecientes(int limit);
}
