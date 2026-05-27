# Integración JavaFX con core embebido

## Flujo

```text
FXML / Vista
→ Controller JavaFX
→ ViewModel / FormState
→ UseCase / ApplicationService
→ OperationResult
→ Feedback visual
```

## Regla

El controller lee campos, valida formato mínimo de UI, llama al core y muestra resultado.

No debe ejecutar SQL, calcular reglas de stock, decidir vencimientos ni manejar licencia directamente.

## Componentes customizados

Los componentes customizados deben ser reutilizables y no conocer SQLite:

- botón primario;
- campo de búsqueda;
- tabla con estado vacío;
- badge de bajo stock;
- diálogo de confirmación;
- panel de resumen;
- toast/alerta;
- selector de archivo/imagen.
