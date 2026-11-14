# First Shopping List Flow Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** Crear/consultar listas propias con persistencia MySQL verificada.
**Architecture:** Monolito modular; dominio puro, aplicación CQRS, repositorios/assemblers JPA e interfaces REST. Identidad basada en Principal, sin adaptador ficticio de producción.
**Tech Stack:** Java 27, Spring Boot 4.1.2, MySQL 8.4; H2 solo para tests rápidos.
**Spec:** ../specs/2026-11-13-shopping-list-first-flow.md

## Global Constraints
- Conservar cambios locales de base, frontend e informe.
- Sin ACL, IAM parcial, cuentas inventadas, credenciales versionadas, commit o push.
- MySQL portable solo loopback/3307; pruebas destructivas únicamente preciazo_shopping_test.

## Review Focus
- Nombre/moneda inválidos no persisten ni publican eventos.
- OwnerId nunca procede del cuerpo/header del cliente; anónimo -> 401, ajeno -> 403.
- Lecturas no generan eventos; evento creado tiene ID persistido y no se duplica.
- Orden y mapeo de Resource coinciden con el contrato; dominio no lleva JPA.
- MySQL real y H2 ejecutan el mismo contrato; el usuario de app tiene privilegios limitados a su esquema.

## Task 1: Pruebas de contrato
**Files:** ShoppingListTest.java, ShoppingListApiTest.java; test fixtures de reloj/eventos y datasource MySQL dedicado.
**Interfaces:** ShoppingList.create/restore/recordPersistence; CreateShoppingListCommand; servicios handle; repositorio findById/findAllByOwnerId/save; ApplicationErrorException.error().
- [x] Escribir pruebas y ejecutar RED por ausencia de implementación.

## Task 2: Flujo vertical
**Files:** shopping/domain/{model,repositories}, application/{commandservices,queryservices,internal}, infrastructure/persistence/jpa, interfaces/rest/{resources,transform,security}; Shared errors/messages.
- [x] Implementar dominio/servicios/JPA/controller y mapeos 401/403 con mensajes localizados.
- [x] Ejecutar el contrato completo en H2 hasta GREEN; conservar 17 pruebas anteriores.

## Task 3: MySQL real y entrega
**Files:** scripts de arranque/configuración local y prueba MySQL, README, docs/architecture.md.
- [x] Preparar instancia local y usuarios/esquemas aislados; probar contrato contra MySQL real.
- [x] Verificar persistencia tras reiniciar la app/instancia; clean verify, JAR y diff --check.
- [x] Documentar límites de IAM y siguiente incremento; dejar sin commit/push.

## Execution Ledger
- No se detectó servidor MySQL ni credenciales en entorno. Se prepara instancia portable aislada, sin servicios globales.
- Ruling: máximo 255 en nombre por tamaño explícito de columna; moneda normalizada y validada con Currency ISO 4217.
- Ruling: primer incremento crea/consulta listas; no simula autenticación. Principal solo inyectado por tests hasta implementar IAM.

- RED observado: testCompile falla solo por paquetes Shopping aún ausentes, antes de implementar el flujo.

- GREEN H2: 10 pruebas nuevas, 0 fallos.
- GREEN MySQL: 7 pruebas del contrato API contra MySQL 8.4.13 real y esquema aislado.
- Persistencia: marcador de prueba confirmado sobrevivió al reinicio de la instancia; tabla auxiliar retirada después.
- Fix verificado: el sondeo inicial tolera conexión rechazada durante arranque en Windows PowerShell, sin ocultar errores de inicialización reales.

- Final GREEN: Maven Wrapper clean verify, 27 tests sin fallos/errores/omisiones, JAR ejecutable generado.
- Runtime dev real: JAR en loopback/18080 con MySQL, health UP, OpenAPI Shopping, POST anónimo 401 y esquema operacional sin fixtures; proceso Java temporal detenido.
- QA cliente: Windows PowerShell requería Accept application/json/decodificar bytes del JSON vendor de Actuator; no fue un fallo de arranque de la aplicación.
- Entrega local sin commit/push; MySQL queda en loopback/3307 y las credenciales/datos excluidos de Git.
