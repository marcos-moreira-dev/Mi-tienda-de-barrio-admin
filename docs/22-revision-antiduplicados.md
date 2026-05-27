# Revisión antirredundancia y duplicados

## Propósito

El proyecto tiene documentación en varias capas. Este documento define cómo evitar contradicciones.

## Duplicados aceptables

Es aceptable repetir una idea cuando cambia el público:

- README raíz: visión ejecutiva.
- docs generales: decisión de producto.
- módulo específico: aplicación concreta.
- database/docs: traducción a tablas.
- desktop/docs: traducción a pantallas.
- comercial: traducción a venta.

## Duplicados peligrosos

No repetir con versiones distintas:

- alcance de V1;
- precio o modelo comercial;
- si ventas internas reemplazan o no facturación;
- si caja/fiado son obligatorios u opcionales;
- si SQLite permite o no varias computadoras;
- si la licencia bloquea datos o solo limita operaciones.

## Fuente de verdad por tema

| Tema | Fuente principal |
|---|---|
| Nombre y promesa | `README.md` |
| Alcance V1 | `docs/03-alcance-vendible.md` |
| Consistencia V1 | `docs/18-matriz-consistencia-v1.md` |
| Modelo de datos conceptual | `docs/06-modelo-de-datos-general.md` |
| Modelo físico | `database/docs/03-tablas-y-campos.md` |
| UX/Desktop | `desktop/docs/01-arquitectura-desktop-javafx.md` |
| Licencia | `docs/04-modelo-comercial-y-licenciamiento.md` |
| Respaldo | `docs/11-seguridad-local-y-respaldos.md` |
| Ventas/cliente | `docs/comercial/` |

## Regla de corrección

Cuando un documento contradiga una fuente principal:

1. No corregir solo el documento secundario.
2. Revisar si la fuente principal también debe cambiar.
3. Actualizar el índice o estado documental si la decisión cambia.
4. Evitar parches silenciosos.

## Revisión final antes de base de datos

Antes de crear SQL, confirmar:

- módulos oficiales V1;
- entidades oficiales V1;
- entidades opcionales;
- campos que dependen de configuración;
- reglas de stock;
- límites tributarios;
- límites de licencia.
