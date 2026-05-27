# Tanda 6 — Salidas / Ventas internas

## Objetivo

Implementar el flujo de salida operativa de stock por venta interna no tributaria.

## Alcance implementado

- Dominio `VentaInterna`.
- Enum `MetodoPagoVentaInterna`.
- Comando `RegistroVentaInternaSimple`.
- Puerto `VentaInternaRepository`.
- Servicio `VentaInternaService`.
- Adaptador `SqliteVentaInternaRepository`.
- Vista JavaFX `VentasInternasView`.
- Registro de salida simple de un producto.
- Validación de stock suficiente.
- Cálculo de total: cantidad × precio unitario.
- Método de pago simple.
- Referencia opcional.
- Advertencia tributaria obligatoria.
- Actualización transaccional de stock.
- Creación automática de movimiento `SALIDA_VENTA_INTERNA`.

## Decisión de diseño

Este módulo no reemplaza facturación electrónica, notas de venta ni obligaciones tributarias. Su propósito es control interno de inventario.

## Estado

Implementado como base funcional. Futuro: venta multiproducto, comprobante interno imprimible y relación con caja.
