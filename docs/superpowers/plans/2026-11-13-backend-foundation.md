# Backend Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** Crear una base Spring Boot local verificable para Preciazo.
**Architecture:** Monolito modular, dominio sin JPA, infraestructura separada y contratos CQRS por contexto. Solo Shared tiene comportamiento en esta entrega.
**Tech Stack:** Java 27, Spring Boot 4.1.2, Maven 3.10.0, MySQL; H2 solo test; springdoc 3.2.0.
**Spec:** ../specs/2026-11-13-backend-foundation.md

## Global Constraints
- Respetar Learning Center ADR-0001/0002/0005/0007; sin ACL.
- No modificar frontend/informe, configuración global, credenciales o repositorios remotos.
- No atribuir funcionalidad de IAM ni compras a la base.

## Review Focus
- Fallos de Result no ejecutan el mapper de éxito.
- Errores 500/JSON inválido no filtran mensajes o valores de entrada.
- Eventos no se publican otra vez después de limpiar; no se promete entrega durable.
- Entorno test no usa credenciales o servicios externos; producción no expone Swagger/health details.

## Task 1: Contratos Shared y errores HTTP
**Files:** pom.xml; shared/application/result/{Result,ApplicationError}.java; shared/interfaces/rest/transform/{ProblemDetailFromApplicationErrorAssembler,ResponseEntityAssembler}.java; tests Shared.
**Interfaces:** Result.success/failure/map/flatMap/mapError/recover/toOptional/getOrElse; ApplicationError(code,message,details) y factories de la guía; assembler de Result con mapper de Resource explícito.
- [x] Escribir tests de composición y mapeo/localización de errores; ejecutar y registrar fallo previo a implementación.
- [x] Implementar los contratos y sus Resources de error; verificar tests verdes.

## Task 2: Eventos y HTTP de base
**Files:** shared/domain/model/aggregates/AbstractDomainAggregateRoot.java; shared/infrastructure/events/DomainEventPublisher.java; shared/interfaces/rest/errors/GlobalExceptionHandler.java; application*.properties; messages*.properties; tests HTTP/eventos.
**Interfaces:** registerDomainEvent/domainEvents/clearDomainEvents; publishAndClearEvents; ProblemDetail para fallos MVC e inesperados.
- [x] Añadir tests fallidos de publicación/limpieza y HTTP con test controllers no distribuidos en producción.
- [x] Implementar la base, validar health/OpenAPI/400/404/500 mediante servidor HTTP real con H2 test.

## Task 3: Reproducibilidad y entrega local
**Files:** README.md; .gitignore; .env.example; package-info.java por contexto; docs/architecture.md; wrapper.
- [x] Fijar versiones/checksums y documentar variables/profiles/patrones de repositorios.
- [x] Ejecutar mvnw clean verify, revisar artefacto y diff, registrar limitación de MySQL real.
- [x] Inicializar Git en develop; dejar cambios sin commit y sin origin hasta publicación autorizada.

## Execution Ledger
- Proyecto generado desde Spring Initializr oficial; generación/configuración es scaffolding.
- MySQL confirmado por el usuario; no se instalarán servicios globales.
- Ruling: OpenAPI usa configuración de perfil y errores MessageSource inyectado; evita estado estático global y preserva contratos de la guía.

- RED: Maven testCompile falla por ausencia de Result/ApplicationError/assemblers/aggregate/publisher; sin errores de sintaxis en tests. Registro local: ../.tools/backend-red.log.

- GREEN Shared: 12 tests, sin fallos.
- RED HTTP: 4 tests, 3 fallos esperados: título OpenAPI, código de validación y respuesta 500 saneada. El contexto JPA/H2 arrancó correctamente.

- GREEN final: Maven Wrapper clean verify; 17 tests, 0 failures/errors/skips; executable JAR generated.
- Verified jar: MySQL driver present; H2/test controllers/test properties absent.
- Environment ruling: HTTP tests require execution outside the restricted network sandbox; initial loopback denial was an environment failure, not a change to application behavior.
- Limitation: no real MySQL connection or production deployment validated; configure server/credentials next.
- Delivery: local develop repository, no commit/origin/push; frontend/report untouched.
