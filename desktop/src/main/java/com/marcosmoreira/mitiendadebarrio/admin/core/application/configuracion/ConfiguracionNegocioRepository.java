package com.marcosmoreira.mitiendadebarrio.admin.core.application.configuracion;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.configuracion.ConfiguracionNegocio;
import java.util.Optional;

/** Puerto de persistencia para la configuración del negocio. */
public interface ConfiguracionNegocioRepository {
    Optional<ConfiguracionNegocio> findCurrent();
    void save(ConfiguracionNegocio configuracion);
}
