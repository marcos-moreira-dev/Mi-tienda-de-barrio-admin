package com.marcosmoreira.mitiendadebarrio.admin.core.application.casosdeuso;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de aplicación de solo lectura para el catálogo operativo de casos de uso.
 *
 * <p>La UI JavaFX consume este servicio para presentar guías de capacitación,
 * pero el catálogo vive en la capa de aplicación porque representa capacidades
 * del sistema y no controles visuales.</p>
 */
public final class UseCaseCatalogService {

    private final List<UseCaseCatalog.UseCaseModule> modules;

    public UseCaseCatalogService() {
        this.modules = UseCaseCatalog.modules();
    }

    public List<UseCaseCatalog.UseCaseModule> modules() {
        return modules;
    }

    public int totalCases() {
        return modules.stream().mapToInt(module -> module.cases().size()).sum();
    }

    public Optional<UseCaseCatalog.UseCaseModule> firstModule() {
        return modules.stream().findFirst();
    }

    public Optional<UseCaseCatalog.UseCaseItem> firstCaseOf(UseCaseCatalog.UseCaseModule module) {
        if (module == null || module.cases().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(module.cases().get(0));
    }

    public Optional<UseCaseCatalog.UseCaseItem> findByCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String normalized = code.trim();
        return modules.stream()
                .flatMap(module -> module.cases().stream())
                .filter(item -> item.code().equalsIgnoreCase(normalized))
                .findFirst();
    }
}
