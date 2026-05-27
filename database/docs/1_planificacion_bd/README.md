# 1. Planificación BD

Este bloque define la base semántica antes de escribir SQL.

Aquí se decide:

- qué objetos del dominio son nucleares;
- qué objetos deben quedar como catálogos;
- qué operaciones deben quedar historizadas;
- qué decisiones se toman para SQLite local;
- qué se deja físicamente listo aunque la UI lo active después;
- y qué límites evitan convertir el producto en un ERP.

La salida de esta carpeta alimenta directamente el SQL bruto, la normalización y la migración oficial.
