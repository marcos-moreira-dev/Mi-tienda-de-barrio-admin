package com.marcosmoreira.mitiendadebarrio.admin.core.domain.venta;

/** Métodos de pago simples para ventas internas no tributarias. */
public enum MetodoPagoVentaInterna {
    EFECTIVO("EFECTIVO", "Efectivo"),
    TRANSFERENCIA("TRANSFERENCIA", "Transferencia"),
    FIADO("FIADO", "Fiado"),
    OTRO("OTRO", "Otro");

    private final String dbValue;
    private final String label;

    MetodoPagoVentaInterna(String dbValue, String label) { this.dbValue = dbValue; this.label = label; }
    public String dbValue() { return dbValue; }
    public String label() { return label; }
    public static MetodoPagoVentaInterna fromDb(String value) {
        for (MetodoPagoVentaInterna metodo : values()) { if (metodo.dbValue.equals(value)) return metodo; }
        return EFECTIVO;
    }
}
