package com.marcosmoreira.mitiendadebarrio.admin.core.application.dashboard;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.dashboard.DashboardResumen;

/** Puerto de lectura para el tablero operativo local. */
public interface DashboardRepository {
    DashboardResumen resumenBase(String estadoLicencia);
}
