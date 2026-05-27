# Transición de SQLite a PostgreSQL

## Decisión V1

La V1 usa SQLite porque es una aplicación autocontenida de una sola computadora.

SQLite es la base oficial mientras el negocio opere así:

- una computadora;
- un usuario operativo principal o pocos usuarios locales;
- sin acceso remoto;
- sin varias cajas simultáneas;
- sin facturación electrónica integrada;
- sin auditoría fuerte.

## Cuándo migrar

Migrar a PostgreSQL cuando aparezca:

- varias computadoras;
- varias sucursales;
- acceso remoto;
- roles fuertes;
- auditoría fuerte;
- facturación electrónica/SRI;
- app móvil;
- tienda online;
- integración con hardware o servicios externos;
- necesidad de disponibilidad centralizada.

## Arquitectura posterior

```text
Cliente JavaFX o web
→ Backend centralizado
→ PostgreSQL
```

## Cómo preparar la migración desde V1

- Mantener SQL V001 documentado.
- Mantener diccionario de datos.
- Evitar nombres físicos ambiguos.
- Usar IDs estables.
- Separar core de SQLite mediante repositorios.
- Crear exportaciones de datos.
- Documentar cambios de esquema.

La transición no debe venderse como “actualización menor”. Es una nueva etapa técnica y comercial.
