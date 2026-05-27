# T56 - Nivelación sidebar y casos de uso contra patrón MMS

## Objetivo
Alinear el comportamiento visual del sidebar y del módulo Casos de uso con el patrón observado en Marcos Moreira Sistema Desktop.

## Cambios
- Sidebar expandido con ancho suficiente para leer nombre y subtítulo.
- Sidebar colapsado con iconos centrados, sin texto y con botón de cierre de sesión como icono.
- Botón de cierre de sesión con estilo de footer oscuro, sin botón fantasma adicional de salir.
- Scrollbars globales con estilo del sistema base.
- Casos de uso convertidos en hub operativo: selector de módulos, selector de casos y detalle documentado.
- Catálogo de casos separado en `UseCaseCatalog` para no meter la data directamente en la vista.

## Fuente documental
- `docs/07-casos-de-uso-generales.md`
- `desktop/docs/manual-usuario/00_matriz_de_casos_de_uso.md`

## Nota
El botón de salir del programa queda disponible en el MenuBar. El sidebar conserva únicamente `Cerrar sesión`.
