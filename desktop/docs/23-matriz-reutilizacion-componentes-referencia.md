# Matriz de reutilización de componentes de referencia

## Referencias usadas

- `Admin-Patterns-lab-main`
- `Marcos-Moreira-admin-Back-Desktop-main`

## Criterio

No se copia todo literalmente. Se adapta lo que aporta al producto autocontenido:

- shell;
- workspace;
- loading;
- login;
- componentes reutilizables;
- familias de pantallas;
- documentación por módulo;
- ayuda contextual;
- diálogos de texto largo;
- patrones CRUD, bandeja, wizard y reportes.

## Componentes conceptuales adaptados

| Referencia | Concepto | Adaptación en Mi tienda de barrio admin |
|---|---|---|
| Admin Patterns Lab | `SharedUiFactory` | Componentes concretos: `AppButton`, `AppDialog`, `InfoPanel`, `SectionHeader` |
| Admin Patterns Lab | Workspace intercambiable | `MainShellView` con `StackPane workspace` |
| Admin Patterns Lab | Variantes por familia de módulo | Documentado en `22-plan-ux-ui-inteligente.md` |
| Marcos Moreira Admin Desktop | Login/loading | `LoadingView` y `LoginView` como flujo inicial |
| Marcos Moreira Admin Desktop | Arquitectura transversal UI | `components/` + `screens/` + CSS global |
| Marcos Moreira Admin Desktop | Ayuda contextual | `InfoPanel` y `AppDialog` |
| Marcos Moreira Admin Desktop | Módulos documentados por propósito, casos de uso y feedback | Documentación de `docs/modulos/` y `desktop/docs/` |

## Decisiones

### No usar FXML todavía

El proyecto referencia `Admin Patterns Lab` usa FXML en el shell. En este proyecto se mantiene UI programática en la primera etapa para acelerar iteración con IA y evitar fricción de sincronización entre controlador y FXML.

### No copiar dominio doméstico

El dominio del laboratorio es pedagógico; aquí solo se toma el patrón visual y de interacción.

### No usar backend HTTP

Aunque el proyecto personal tiene integración desktop/backend, aquí se reemplaza por core embebido porque el sistema es autocontenido con SQLite.

## Componentes obligatorios antes de módulos pesados

- `AppButton`
- `AppCard`
- `SectionHeader`
- `StatusBadge`
- `EmptyState`
- `AppDialog`
- `ActionBar`
- `FormGrid`
- `InfoPanel`
- `ModuleScaffold`

## Regla para futuras tandas

Cada módulo nuevo debe declarar:

1. familia de pantalla;
2. componentes transversales usados;
3. flujo UI → core → SQLite;
4. mensajes y feedback;
5. qué se reutiliza de la referencia.
