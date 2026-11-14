# Base del backend Preciazo

Alcance autorizado: preparar un repositorio local separado del frontend Angular y del informe.
Java 27, Spring Boot 4.1.2, Maven Wrapper 3.10.0 y MySQL, según Learning Center.
Paquete raíz: com.preciazo.platform. Arquitectura: monolito modular con siete contextos.
Esta entrega proporciona arranque, configuración dev/prod, OpenAPI, Result/ApplicationError,
ProblemDetail localizable, traducción de Resources y publicación/limpieza de eventos.
Los aggregates no llevan JPA; sus adaptadores de persistencia publicarán después de guardar.
IAM, JWT, listas, ACL y proveedores externos quedan para entregas posteriores.

El desarrollo usa MySQL mediante variables de entorno, sin credenciales predeterminadas.
H2 se usa exclusivamente en tests para verificar contexto JPA, HTTP y documentación sin
instalar servicios globales. La conexión real a MySQL requiere servidor y credenciales del equipo.
No se crean endpoints de negocio ficticios, tablas vacías o modelos compartidos entre contextos.
No se publica repositorio ni se hace commit/push como parte de preparar esta base local.

Criterios: wrapper reproducible; tests de composición/errores, eventos y HTTP; mvnw verify verde;
HTTP real con perfil test, health UP, OpenAPI disponible y errores sin datos sensibles.
