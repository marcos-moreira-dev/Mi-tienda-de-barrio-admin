# Tanda 1 — Validación de compilación local

## Objetivo

Dejar una validación rápida para comprobar que el núcleo local de MiTienda compila con Java 21 sin depender de JavaFX.

## Cambios realizados

Se agregaron scripts:

```text
scripts/validate-core-no-javafx.bat
scripts/validate-core-no-javafx.ps1
```

Estos scripts compilan con `javac --release 21` las capas:

```text
bootstrap
core/application
core/domain
core/infrastructure
shared
```

y excluyen:

```text
MiTiendaDeBarrioAdminApp.java
desktop/ui/**
```

porque esas clases requieren dependencias JavaFX/Maven.

## Resultado en esta tanda

En el entorno de trabajo se validó que el subconjunto no JavaFX compila correctamente con Java 21.

## Uso recomendado en Windows

Desde la raíz del proyecto:

```powershell
.\scripts\validate-core-no-javafx.bat
```

Para validación completa de JavaFX/Maven, seguir usando:

```powershell
.\scripts\validate-desktop.bat
```

## Decisión

Esta validación no reemplaza Maven, pero sirve como prueba rápida para detectar clases faltantes del core antes de ejecutar la validación completa.
