# Testing del core

Pruebas mínimas:

- registrar producto válido;
- rechazar producto sin nombre;
- registrar compra y actualizar stock;
- registrar venta interna y descontar stock;
- impedir stock negativo si la regla está activa;
- registrar merma;
- generar productos por comprar;
- crear respaldo;
- validar licencia vigente/vencida;
- ejecutar smoke check de base.

Estrategia: unitarias para dominio, aplicación para casos de uso, integración SQLite con base temporal y pruebas manuales desde JavaFX cuando exista UI.
