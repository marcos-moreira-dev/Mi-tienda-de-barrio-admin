package com.marcosmoreira.mitiendadebarrio.admin.core.application.dashboard;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.dashboard.DashboardResumen;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.license.LocalLicenseService;

/** Caso de uso de lectura para la pantalla de inicio. */
public final class DashboardService {
    private final DashboardRepository repository;
    private final LocalLicenseService licenseService;

    public DashboardService(DashboardRepository repository, LocalLicenseService licenseService) {
        this.repository = repository;
        this.licenseService = licenseService;
    }

    public DashboardResumen resumen() {
        return repository.resumenBase(licenseService.currentStatus().label());
    }
}
