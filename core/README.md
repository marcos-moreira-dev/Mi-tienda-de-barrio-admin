# Core embebido

El core embebido es el núcleo interno de la aplicación autocontenida **Mi tienda de barrio admin**.

No es un backend HTTP. No expone API REST. No escucha puertos. No requiere servidor.

Su función es aislar la lógica de negocio de la interfaz JavaFX y de SQLite.

```text
JavaFX Controller
→ Caso de uso
→ Servicio de aplicación
→ Repositorio SQLite
→ Base local
```

Ver `core/docs/00-indice-core.md`.
