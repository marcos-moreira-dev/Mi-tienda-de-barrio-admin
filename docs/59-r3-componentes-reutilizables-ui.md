# R3 — Componentes reutilizables UI

## Objetivo

Reducir repetición en pantallas JavaFX y dejar una base más profesional para seguir construyendo módulos pesados sin volver cada vista una mezcla de layout, estilo y reglas visuales.

## Implementado

Se agregaron componentes/fábricas transversales en:

```text
desktop/src/main/java/com/marcosmoreira/mitiendadebarrio/admin/desktop/ui/components/
```

Nuevos archivos:

- `AppFormFactory.java`: creación consistente de campos de formulario (`TextField`, `PasswordField`, `TextArea`, `ComboBox`, `DatePicker`, `CheckBox`).
- `AppListFactory.java`: listas visuales reutilizables para bullets y pasos numerados.
- `AppScrollFactory.java`: scroll vertical, horizontal y combinado con políticas homogéneas.
- `AppDialogService.java`: servicio simple para centralizar diálogos y permitir inyección futura.

## Refactor aplicado

La pantalla `CasosDeUsoView` ya consume:

- `AppScrollFactory` para los scrolls de módulos y chips de casos.
- `AppListFactory` para pasos numerados.

Esto reduce construcción manual de controles y deja el patrón listo para replicarse en productos, compras, salidas, caja y fiado.

## Criterios de uso para siguientes tandas

Toda pantalla nueva o refactorizada debe intentar usar primero:

- `ModuleScaffold` para estructura general.
- `SectionHeader` para encabezados.
- `AppCard` para bloques visuales.
- `InfoPanel` para ayuda lateral.
- `ActionBar` para botones de acción.
- `AppFormFactory` para campos.
- `AppListFactory` para listas y pasos.
- `AppScrollFactory` para áreas desplazables.

## Decisión

No se introduce FXML todavía. La UI sigue programática para mantener velocidad de iteración, pero con componentes reutilizables para que el código no crezca desordenado.
