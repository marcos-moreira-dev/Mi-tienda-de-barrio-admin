package com.marcosmoreira.mitiendadebarrio.admin.core.domain.compra;

/** Tipos de comprobante referenciales para compras internas. */
public enum TipoComprobanteCompra {
    SIN_COMPROBANTE("SIN_COMPROBANTE", "Sin comprobante"),
    NOTA("NOTA", "Nota"),
    FACTURA("FACTURA", "Factura"),
    OTRO("OTRO", "Otro");

    private final String dbValue;
    private final String label;

    TipoComprobanteCompra(String dbValue, String label) { this.dbValue = dbValue; this.label = label; }
    public String dbValue() { return dbValue; }
    public String label() { return label; }
    public static TipoComprobanteCompra fromDb(String value) {
        for (TipoComprobanteCompra tipo : values()) { if (tipo.dbValue.equals(value)) return tipo; }
        return SIN_COMPROBANTE;
    }
}
