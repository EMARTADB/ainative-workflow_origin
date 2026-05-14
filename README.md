# Acelerador de Ciclo de Vida del Software con Sistema Agentico

## Descripcion

Este proyecto demuestra como acelerar el desarrollo del ciclo de vida del software mediante un **sistema agentico** basado en una arquitectura de **orquestador + subagentes**. El sistema aprovecha las funcionalidades de [OpenCode](https://opencode.ai) y se apoya en el estandar [OpenSpec](https://openspec.ai) para gestionar cambios de forma estructurada y trazable.

## Como funciona

El sistema agentico se compone de un pipeline de subagentes especializados, cada uno responsable de una fase del ciclo de vida:

1. **Requisitos funcionales** -- Descubre, clarifica y documenta los requisitos funcionales del cambio.
2. **Requisitos tecnicos** -- Define los requisitos no funcionales y restricciones tecnicas.
   - **2.5. Revision de requisitos** -- Permite al usuario revisar y ajustar los requisitos antes de continuar.
3. **Definicion** -- Genera automaticamente los artefactos de definicion: propuesta, especificaciones, diseno y tareas.
4. **Construccion** -- Implementa las tareas pendientes, realizando cambios de codigo y marcando tareas completadas.
5. **Validacion** -- Compila la aplicacion, la arranca y ejecuta validacion funcional con Playwright contra los requisitos.
6. **Archivo** -- Valida la completitud del cambio y archiva los artefactos en OpenSpec.

El orquestador coordina la ejecucion secuencial de estos subagentes, asegurando que cada fase se complete antes de avanzar a la siguiente.

## Tecnologias Utilizadas

- **OpenCode** -- Herramienta CLI agentica que proporciona la infraestructura de orquestacion y subagentes.
- **OpenSpec** -- Estandar para la gestion estructurada de especificaciones y cambios en proyectos software.
- **Playwright** -- Utilizado en la fase de validacion para pruebas funcionales end-to-end via navegador.


## Aplicacion de pruebas: PetClinic

Las pruebas se han realizado sobre **Spring PetClinic**, una aplicacion de referencia basada en Spring Framework 7.x con configuracion XML, arquitectura de 3 capas y despliegue como WAR.

## Cambios testados

En la carpeta `openspec/` se pueden consultar todos los cambios testados con el sistema agentico. Los cambios completados y archivados se encuentran en `openspec/changes/archive/`:

| Cambio | Fecha | Descripcion |
|--------|-------|-------------|
| `pet-microchip-id` | 2026-05-06 | Gestion de identificador de microchip para mascotas |
| `pet-transfer` | 2026-05-07 | Transferencia de mascotas entre propietarios |
| `vet-visit-assignment` | 2026-05-08 | Asignacion de veterinarios a visitas |

Cada cambio archivado contiene los artefactos completos generados por el pipeline: requisitos funcionales, requisitos tecnicos, propuesta, diseno, tareas y resultados de validacion.
