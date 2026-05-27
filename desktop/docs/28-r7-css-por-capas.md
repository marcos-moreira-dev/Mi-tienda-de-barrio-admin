# 28 · R7 · CSS por capas

La UI ya no depende de una sola hoja gigante de estilos. La carga se centraliza en:

```text
com.marcosmoreira.mitiendadebarrio.admin.desktop.ui.theme.ThemeStylesheets
```

Orden de carga:

1. `tokens.css` — identidad visual y variables.
2. `base.css` — escena y contenedores base.
3. `controls.css` — controles JavaFX nativos.
4. `components.css` — componentes reutilizables de la app.
5. `login.css` — login y loading.
6. `shell.css` — sidebar, topbar, menubar, statusbar y workspace.
7. `use-cases.css` — hub de casos de uso.
8. `modules.css` — ajustes por módulo de negocio.

## Regla para futuras tandas

No agregar estilos nuevos en `app.css`. Ese archivo queda como compatibilidad/documentación.

Si el estilo es transversal, va a `components.css` o `controls.css`.
Si afecta al marco de la app, va a `shell.css`.
Si afecta a login, va a `login.css`.
Si afecta a casos de uso, va a `use-cases.css`.
Si afecta solo a un módulo de negocio, va a `modules.css`.
