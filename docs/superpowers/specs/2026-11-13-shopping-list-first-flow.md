# Primer flujo de listas con MySQL

Alcance: crear lista (US08), listar listas propias y consultar detalle propio. Java 27/Spring Boot 4.1.2/MySQL, conservando la base y sus 17 pruebas.
Crear recibe nombre opcional y moneda ISO 4217; nombre ausente/blanco usa Lista yyyy-MM-dd HH:mm:ss según Clock en America/Lima. Nombre máximo 255 caracteres, consistente con el almacenamiento. La nueva lista es ACTIVE, versión inicial 0, con createdAt e ID generado al guardar.
El ownerId se obtiene exclusivamente de un Principal confiable del servidor; sin principal o con identificador inválido se devuelve 401. No se incorpora IAM ni un header/cuenta ficticios para producción. Las pruebas inyectan un Principal exclusivamente en MockMvc. Hasta IAM las solicitudes públicas quedan cerradas.
POST /api/v1/shopping-lists -> 201, Location, ETag y Resource. GET /api/v1/shopping-lists -> 200, solo propias y orden createdAt DESC/id DESC. GET /api/v1/shopping-lists/{id} -> 200 propia, 403 ajena, 404 inexistente. Validación -> 400; recursos/errors sin secretos.
Dominio sin JPA; Commands/Queries records, servicios separados, repositorio puerto y adaptador/assembler/entidad separados. Command devuelve Result<Long, ApplicationError>. Evento ShoppingListCreated registrado al asignar ID real y publicado/limpiado después del save/flush, sin garantía durable.
Preparar instancia MySQL local en workspace, puerto 3307/loopback, sin servicios globales ni tocar bases existentes. Credenciales aleatorias en archivos ignorados, nunca chat/logs/versionado. Tests MySQL solo contra esquema dedicado preciazo_shopping_test; no usar una base del equipo para create-drop.
No implementar todavía artículos, presupuesto, favoritos, plantillas, sustitutos, ACL o autenticación. No commit/push.
