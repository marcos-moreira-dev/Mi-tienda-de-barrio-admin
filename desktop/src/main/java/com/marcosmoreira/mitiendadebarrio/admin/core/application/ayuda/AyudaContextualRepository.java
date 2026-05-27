package com.marcosmoreira.mitiendadebarrio.admin.core.application.ayuda;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.ayuda.AyudaContextual;
import java.util.List;

/** Puerto de lectura para ayuda contextual local. */
public interface AyudaContextualRepository {
    List<AyudaContextual> findAllVisible();
    List<AyudaContextual> findByModulo(String modulo);
    void ensureDefaultContent();
}
