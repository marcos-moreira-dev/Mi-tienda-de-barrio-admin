package com.marcosmoreira.mitiendadebarrio.admin.core.application.ayuda;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.ayuda.AyudaContextual;
import java.util.List;

/** Casos de uso de ayuda contextual y mini manual interno. */
public final class AyudaContextualService {
    private final AyudaContextualRepository repository;
    public AyudaContextualService(AyudaContextualRepository repository) { this.repository = repository; }
    public List<AyudaContextual> todas() { return repository.findAllVisible(); }
    public List<AyudaContextual> porModulo(String modulo) { return repository.findByModulo(modulo); }
    public void asegurarContenidoBase() { repository.ensureDefaultContent(); }
}
