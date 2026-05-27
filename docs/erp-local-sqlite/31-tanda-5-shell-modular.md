# Tanda 5 — Modularizar shell JavaFX

## Objetivo

Separar responsabilidades de la carcasa principal para que `MainShellView` no siga creciendo cuando se agreguen módulos ERP locales.

La tanda no cambia la experiencia visible del usuario. Cambia la organización interna de navegación y construcción de pantallas.

## Cambios implementados

Se agregaron las siguientes piezas:

```text
AppModuleGroupDescriptor
ModuleViewFactory
WriteModulePolicy
ModuleIconResolver
```

También se extendió:

```text
AppShellDescriptor
```

Ahora el descriptor no solo contiene la lista de módulos, sino también los grupos de navegación.

## Qué se sacó de MainShellView

Antes `MainShellView` contenía:

```text
grupos de navegación hardcodeados
switch para crear pantallas concretas
switch para saber qué módulos escriben
switch para resolver iconos
```

Ahora delega en:

```text
AppShellDescriptor / AppModuleGroupDescriptor
ModuleViewFactory
WriteModulePolicy
ModuleIconResolver
```

## Resultado

`MainShellView` queda más preparado para crecer con módulos como:

```text
Usuarios
Auditoría
Terceros
Gastos
Cuentas por pagar
Fiscalidad preparada
Contabilidad básica
Activos
Empleados
Importaciones
Checklist
```

sin convertir la shell en una clase gigante.

## Validaciones agregadas

Se agregaron scripts:

```text
scripts/validate-shell-modular.bat
scripts/validate-shell-modular.ps1
```

Validan que:

```text
existan las clases nuevas;
AppShellDescriptor tenga grupos de navegación;
MainShellView use ModuleViewFactory;
MainShellView use WriteModulePolicy;
MainShellView use ModuleIconResolver;
MainShellView renderice grupos desde el descriptor;
MainShellView ya no construya pantallas concretas con el switch anterior;
MainShellView ya no tenga isWriteModule hardcodeado.
```

## Validación ejecutada en esta tanda

En el entorno de trabajo se validó:

```text
shell modular por chequeos estáticos: OK
core sin JavaFX con javac --release 21: OK
```

No se ejecutó Maven completo porque el entorno de trabajo no tiene Maven ni dependencias JavaFX instaladas.

## Comando recomendado en Windows

Desde la raíz del proyecto:

```powershell
.\scripts\validate-shell-modular.bat
.\scripts\validate-core-no-javafx.bat
.\scripts\validate-migrator-local.bat
.\scripts\validate-sql-local.bat
```

Si tienes Maven configurado con Java 21, también conviene ejecutar la validación completa del desktop.

## Decisión

La carcasa JavaFX queda preparada para crecer por módulos sin meter backend, sin Spring y sin PostgreSQL.
