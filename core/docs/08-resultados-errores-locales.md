# Resultados y errores locales

Como no hay API HTTP, no usar `ApiResponse<T>` como contrato principal.

Usar conceptos locales:

```text
OperationResult<T>
ValidationResult
PageResult<T>
AppError
BusinessException
ValidationException
NotFoundException
ConflictException
```

La UI muestra mensajes humanos; los detalles técnicos se guardan en logs.
