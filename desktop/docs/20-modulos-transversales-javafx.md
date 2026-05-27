# Módulos transversales JavaFX

## Propósito

Definir los bloques visuales reutilizables antes de construir pantallas de negocio.

## Decisión

Los transversales JavaFX deben existir temprano porque controlan la experiencia de arranque, navegación, errores y confianza visual del sistema.

## Bloques transversales

1. Loading / arranque.
2. Login local.
3. Shell principal.
4. Componentes customizados.
5. Mensajes y diálogos.
6. Tema visual y CSS.

## Componentes base

- AppButton.
- AppCard.
- SectionHeader.
- StatusBadge.
- EmptyState.

## Regla

Primero se estabilizan los transversales. Luego se implementan módulos. Eso evita que cada pantalla invente botones, estilos, diálogos y estados vacíos propios.
