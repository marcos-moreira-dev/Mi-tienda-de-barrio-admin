package com.marcosmoreira.mitiendadebarrio.admin.core.domain.auditoria;

/** Evento de auditoría local. Guarda trazabilidad humana sin depender de nube ni backend. */
public record AuditoriaEvento(
        Long id,
        Long usuarioId,
        String fechaEvento,
        String modulo,
        String accion,
        String entidad,
        Long entidadId,
        String resumen,
        String detalleJson,
        ResultadoAuditoria resultado
) {
    public AuditoriaEvento {
        if (modulo == null || modulo.isBlank()) {
            throw new IllegalArgumentException("El módulo de auditoría es obligatorio.");
        }
        if (accion == null || accion.isBlank()) {
            throw new IllegalArgumentException("La acción de auditoría es obligatoria.");
        }
        if (resumen == null || resumen.isBlank()) {
            throw new IllegalArgumentException("El resumen de auditoría es obligatorio.");
        }
        if (resultado == null) {
            resultado = ResultadoAuditoria.OK;
        }
    }

    public static AuditoriaEvento nuevo(
            Long usuarioId,
            String modulo,
            String accion,
            String entidad,
            Long entidadId,
            String resumen,
            String detalleJson,
            ResultadoAuditoria resultado
    ) {
        return new AuditoriaEvento(null, usuarioId, null, modulo, accion, entidad, entidadId, resumen, detalleJson, resultado);
    }
}
