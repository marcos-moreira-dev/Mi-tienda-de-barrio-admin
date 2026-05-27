package com.marcosmoreira.mitiendadebarrio.admin.core.application.configuracion;

import com.marcosmoreira.mitiendadebarrio.admin.core.application.license.WriteAccessGuard;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.configuracion.ConfiguracionNegocio;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.ValidationException;
import com.marcosmoreira.mitiendadebarrio.admin.shared.result.OperationResult;

/** Casos de uso del módulo Configuración del negocio. */
public final class ConfiguracionNegocioService {
    private final ConfiguracionNegocioRepository repository;
    private final WriteAccessGuard writeAccessGuard;

    public ConfiguracionNegocioService(ConfiguracionNegocioRepository repository) {
        this(repository, null);
    }

    public ConfiguracionNegocioService(ConfiguracionNegocioRepository repository, WriteAccessGuard writeAccessGuard) {
        this.repository = repository;
        this.writeAccessGuard = writeAccessGuard;
    }

    public ConfiguracionNegocio obtenerActual() {
        return repository.findCurrent().orElseGet(ConfiguracionNegocio::vacia);
    }

    public OperationResult<ConfiguracionNegocio> guardar(ConfiguracionNegocio configuracion) {
        OperationResult<ConfiguracionNegocio> blocked = bloquearSiNoPuedeEscribir("guardar configuración del negocio");
        if (blocked != null) return blocked;
        try {
            validar(configuracion);
            repository.save(configuracion);
            return OperationResult.success(configuracion, "Configuración guardada correctamente.");
        } catch (ValidationException ex) {
            return OperationResult.failure(ex.getMessage());
        }
    }

    private void validar(ConfiguracionNegocio configuracion) {
        if (configuracion.nombreComercial() == null || configuracion.nombreComercial().isBlank()) {
            throw new ValidationException("El nombre comercial es obligatorio.");
        }
        if (configuracion.moneda() == null || configuracion.moneda().isBlank()) {
            throw new ValidationException("La moneda es obligatoria.");
        }
    }

    private <T> OperationResult<T> bloquearSiNoPuedeEscribir(String operacion) {
        if (writeAccessGuard != null && !writeAccessGuard.canWrite()) {
            return OperationResult.failure(writeAccessGuard.limitedModeMessage(operacion));
        }
        return null;
    }
}
