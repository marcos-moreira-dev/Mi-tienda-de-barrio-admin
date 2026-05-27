package com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiado;
import java.math.BigDecimal; import java.time.LocalDateTime;
public record CuentaPorCobrar(Long id, Long clienteFiadoId, Long ventaInternaId, LocalDateTime fechaApertura, BigDecimal montoOriginal, BigDecimal saldoPendiente, EstadoCuentaPorCobrar estado, String observacion) { }
