package com.marcosmoreira.mitiendadebarrio.admin.core.domain.fiado;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.caja.MetodoPagoCaja; import java.math.BigDecimal; import java.time.LocalDateTime;
public record Abono(Long id, Long cuentaPorCobrarId, LocalDateTime fechaAbono, BigDecimal monto, MetodoPagoCaja metodoPago, String observacion) { }
