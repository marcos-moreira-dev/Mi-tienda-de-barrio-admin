# Reglas de negocio

## Stock

Stock actual no debe ser negativo salvo ajuste autorizado. Stock mínimo dispara alerta. Stock objetivo sirve para calcular cantidad sugerida a comprar.

## Compras

Toda compra aumenta stock y genera movimientos. Debe ejecutarse en transacción.

## Salidas

Toda salida disminuye stock y requiere motivo. Venta interna no es facturación.

## Vencimientos

Si un producto maneja vencimiento, debe poder generar alertas de próximo a vencer y vencido. Producto vencido debe retirarse por merma/retiro.

## Reportes

Productos por comprar usa stock actual, stock mínimo y stock objetivo. PDF para imprimir y CSV/Excel para editar.

## Respaldos

Restaurar debe pedir confirmación y sugerir respaldo previo.

## Licencia

Vencimiento no borra datos. Modo limitado permite consulta, exportación y respaldo.
