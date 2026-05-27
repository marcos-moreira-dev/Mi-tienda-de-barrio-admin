package com.marcosmoreira.mitiendadebarrio.admin.core.application.service;

import com.marcosmoreira.mitiendadebarrio.admin.bootstrap.StartupReport;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.license.LocalLicenseService;
import com.marcosmoreira.mitiendadebarrio.admin.core.infrastructure.sqlite.DatabaseHealthCheck;

import java.util.List;

/** Verifica condiciones mínimas de arranque. */
public final class SystemHealthService {

    private final DatabaseHealthCheck databaseHealthCheck;
    private final LocalLicenseService licenseService;

    public SystemHealthService(DatabaseHealthCheck databaseHealthCheck, LocalLicenseService licenseService) {
        this.databaseHealthCheck = databaseHealthCheck;
        this.licenseService = licenseService;
    }

    public StartupReport checkStartup() {
        return new StartupReport(true, List.of(
                databaseHealthCheck.describeStatus(),
                "Licencia: " + licenseService.currentStatus().label()
        ));
    }
}
