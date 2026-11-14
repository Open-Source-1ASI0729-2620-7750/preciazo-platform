# Decisiones de la base

- Learning Center ADR-0001: `Result` y `ApplicationError` para resultados esperados. El mapper de Resource solo se ejecuta en éxito.
- ADR-0002: dominio sin anotaciones JPA; `AbstractDomainAggregateRoot` usa Spring Data Commons, como la guía, sin ser una entidad persistida.
- ADR-0005: `DomainEventPublisher.publishAndClearEvents` se usará en cada adaptador después de guardar y obtener el ID. No es un outbox ni un bus durable. Si un listener falla tras recibir otros eventos, la entrega parcial puede duplicarse al reintentar; los futuros consumidores deberán definir idempotencia.
- ADR-0007: cada capacidad futura separará contratos `handle(Command)` y `handle(Query)`. No se añaden interfaces vacías que aparenten implementar casos de uso.
- Contextos: catalog, routing, shopping, merchant, verification, analytics e iam. Compartir una base física no habilita joins o imports entre dominios.
- MySQL fue elegido por el usuario. H2 existe solo como dependencia de prueba. El contrato de listas se verificó además contra MySQL 8.4.13 real; esto no acredita futuras migraciones no implementadas.
- La firma JWT y las reglas de bloqueo de US06 se implementarán en IAM; no se distribuyen stubs de autenticación o ACL.
- Adaptación de la guía: MessageSource se inyecta en el assembler de errores; no se mantiene un contexto global estático de mensajes. Los mapeos IAM/Shopping explícitos se añadirán con cada funcionalidad, antes de los sufijos genéricos; actualmente existen validación, not-found, conflicto, regla de negocio, inesperado, AUTHENTICATION_REQUIRED (401) y SHOPPING_FORBIDDEN (403).
- JPA open-in-view y SQL logging quedan deshabilitados para que las futuras transacciones residan en aplicación y los controllers usen Resources.
- Dev usa ddl-auto=update como arranque de la guía; producción usa validate y requerirá esquema/migraciones preparados antes de desplegar funciones.
- No se eligió una licencia del proyecto ni se creó repositorio remoto en esta entrega.

## Primer incremento de Shopping

- CreateShoppingListCommand y dos Queries implementan crear/listar/detalle, siguiendo el diseño de US08. Ningún otro flujo de Shopping está implementado todavía.
- ShoppingList es un aggregate sin JPA; la entidad ShoppingListPersistenceEntity declara explícitamente shopping_lists, índice de propietario/fecha e identidad/versión generadas.
- El adaptador guarda y hace flush, asigna el ID persistido al aggregate y después publica/limpia ShoppingListCreated. Reconstituir/consultar no registra otro evento. La transacción de aplicación debe concluir antes de devolver el éxito HTTP; la publicación síncrona no acredita commit durable o entrega externa.
- ApplicationErrorException transporta fallos tipados de Queries/identidad hasta ProblemDetail. Respuestas 401/403 no exponen información del otro comprador.
- CurrentBuyer solo consume Principal confiable del contenedor; sin IAM no se establece uno desde peticiones públicas. Identidades de MockMvc viven exclusivamente en tests, no en una adaptación de desarrollo que pueda publicarse accidentalmente.
- El nombre usa Clock inyectado, zona America/Lima configurable, y moneda validada con Currency. Límite de nombre 255 para coincidir con la columna SQL; no se recorta silenciosamente.
- MySQL portable solo escucha loopback/3307. Usuarios de aplicación/prueba y esquemas están separados; datos/credenciales son ignorados por Git. El esquema de pruebas está restringido por nombre antes de permitir create-drop.
