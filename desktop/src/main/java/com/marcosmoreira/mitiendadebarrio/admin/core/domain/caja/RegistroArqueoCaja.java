package com.marcosmoreira.mitiendadebarrio.admin.core.domain.caja;

import java.math.BigDecimal;

/** Comando para registrar arqueo de caja sin cerrar necesariamente la jornada. */
public record RegistroArqueoCaja(Long cajaDiariaId, BigDecimal saldoContado, String responsableTexto, String observacion) { }
