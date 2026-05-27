package com.marcosmoreira.mitiendadebarrio.admin.core.domain.movimiento;

/** Tipos de movimiento soportados por la V1 local. */
public enum TipoMovimientoInventario {
    ENTRADA_COMPRA("ENTRADA_COMPRA", "Entrada por compra", 1),
    SALIDA_VENTA_INTERNA("SALIDA_VENTA_INTERNA", "Salida por venta interna", -1),
    AJUSTE_POSITIVO("AJUSTE_POSITIVO", "Ajuste positivo", 1),
    AJUSTE_NEGATIVO("AJUSTE_NEGATIVO", "Ajuste negativo", -1),
    MERMA("MERMA", "Merma", -1),
    RETIRO_VENCIMIENTO("RETIRO_VENCIMIENTO", "Retiro por vencimiento", -1),
    CORRECCION("CORRECCION", "Corrección", 0);

    private final String dbValue;
    private final String label;
    private final int sign;

    TipoMovimientoInventario(String dbValue, String label, int sign) {
        this.dbValue = dbValue;
        this.label = label;
        this.sign = sign;
    }

    public String dbValue() { return dbValue; }
    public String label() { return label; }
    public int sign() { return sign; }

    public static TipoMovimientoInventario fromDb(String value) {
        for (TipoMovimientoInventario tipo : values()) {
            if (tipo.dbValue.equals(value)) {
                return tipo;
            }
        }
        return CORRECCION;
    }
}
