# Preciazo Web Services

Base local del backend de Preciazo: Java 27, Spring Boot 4.1.2, Maven Wrapper 3.10.0 y MySQL.
El frontend Angular vive en `Preciazo-platform` y los diseños en `preciazo-report`.

Esta entrega incluye configuración de entorno, JPA, OpenAPI, `Result<T, ApplicationError>`,
Resources/`ProblemDetail` en inglés/español y publicación/limpieza de eventos de dominio.
IAM/JWT, artículos/presupuesto de listas, catálogo y proveedores externos todavía no están implementados.
Shopping Planning ya implementa crear listas, listar las propias y consultar su detalle, con control de propietario.

## Preparación en Windows

Se necesita un JDK 27 completo y MySQL ejecutándose. Maven se descarga mediante el wrapper
con versión y SHA-256 fijos; `JAVA_HOME` debe apuntar a un JDK, no a un JRE.
Las herramientas portátiles usadas en esta carpeta de trabajo están en el directorio hermano `.tools`:

```powershell
$env:JAVA_HOME = (Resolve-Path '..\.tools\java27\jdk-27.0.1.0.3').Path
```

En otro equipo, apunta `JAVA_HOME` a tu instalación local de JDK 27. No se requiere modificarlo globalmente.
Crea una base MySQL `preciazo` y configura un usuario del equipo con acceso a esa base.
La aplicación no instala MySQL ni crea usuarios o contraseñas por defecto.

```powershell
$env:SPRING_PROFILES_ACTIVE = 'dev'
$env:DATABASE_USER = 'preciazo'
$env:DATABASE_PASSWORD = '<tu contraseña local>'
.\mvnw.cmd spring-boot:run
```

`DATABASE_URL` es el host (por defecto localhost), `DATABASE_PORT` vale 3306,
`DATABASE_NAME` vale preciazo y `PORT` vale 8080 si no se definen. `.env.example` documenta
los nombres; Spring no carga `.env` automáticamente. No escribas la contraseña en archivos versionados.

- Estado: `http://localhost:8080/actuator/health`.
- OpenAPI: `http://localhost:8080/v3/api-docs`.
- Swagger: `http://localhost:8080/swagger-ui/index.html`.

## Verificación sin un servidor externo

```powershell
.\mvnw.cmd clean verify
```

Los tests usan el perfil `test` con H2 exclusivamente en `src/test/resources`. Verifican composición
Result, errores localizados, publicación/limpieza de eventos y un servidor HTTP real con JPA,
health, OpenAPI y respuestas 400/404/500 saneadas. No equivalen a verificar migraciones o una
conexión de producción MySQL. El JAR no incorpora H2 ni los controladores de prueba.
En Linux/macOS: `bash mvnw clean verify` con JDK 27 disponible.

## Perfiles

| Perfil | Persistencia | Esquema | Documentación |
| --- | --- | --- | --- |
| dev (predeterminado) | MySQL; usuario/contraseña de entorno | update, como la etapa inicial de la guía | habilitada |
| prod | MySQL mediante DATABASE_JDBC_URL y credenciales de entorno obligatorias | validate; exige esquema preparado | deshabilitada |
| test | H2 en memoria, solo tests | create-drop | habilitada para verificación |

`open-in-view` y el log SQL están deshabilitados. Health no expone detalles de conexión.
Antes de entregar un entorno con funciones privadas, el siguiente incremento debe implementar IAM
con JWT y autorización del servidor, según US06/TS01; esta base no afirma tener esa seguridad.

## Organización

`com.preciazo.platform` contiene `shared`, `catalog`, `routing`, `shopping`, `merchant`,
`verification`, `analytics` e `iam`. Shopping ya contiene dominio, aplicación, persistencia e interfaces REST para el primer flujo; los otros seis contienen límites documentados.
Cada funcionalidad añadirá sus propios `application`, `domain`, `infrastructure` e `interfaces`.
No hay modelos globales compartidos entre contextos ni ACL.

Referencia: `Contexto del Proyecto/learning-center-platform-guide.md` y diseños de `preciazo-report`.
Consulta [decisiones de arquitectura](docs/architecture.md) y el [plan de esta base](docs/superpowers/plans/2026-11-13-backend-foundation.md).

## Verificación de esta entrega

`mvnw.cmd clean verify`: **27 pruebas, 0 fallos/errores/omisiones**, conservando las 17 de la base. El contrato API se ejecutó además contra MySQL real: 7 pruebas, sin fallos.
Se verificó que el JAR incluye el driver MySQL y excluye H2, el perfil test y sus controllers.
El contrato API de listas se ejecutó también contra MySQL real: 7 pruebas, sin fallos.

## Primer flujo de listas (US08)

Implementado: `POST /api/v1/shopping-lists` (201, Location y ETag),
`GET /api/v1/shopping-lists` (solo propias, más recientes primero) y
`GET /api/v1/shopping-lists/{id}` (detalle propio; 403 ajeno, 404 inexistente).
El cuerpo de creación tiene `name` opcional y `currency` obligatoria. El nombre omitido/blanco
se forma como `Lista yyyy-MM-dd HH:mm:ss` en America/Lima; la moneda se valida como ISO 4217.
La lista queda ACTIVE y su ID/versión se obtienen de MySQL. JSON con campos desconocidos se rechaza.

El propietario procede del `Principal` del servidor, nunca de ownerId enviado por el cliente.
**IAM aún no está conectado**: las peticiones anónimas reciben 401. Los tests inyectan
Principal solo en MockMvc para verificar el contrato; no hay headers o cuentas simuladas en producción.
Enviar un bearer token todavía no permite autenticarse. El siguiente incremento debe conectar IAM
antes de probar listas privadas desde Angular/Swagger como un usuario real.

### MySQL local de este workspace

Se preparó MySQL Community 8.4.13 portable en el directorio hermano `.tools/mysql`, escuchando
solo en `127.0.0.1:3307`, sin instalar un servicio Windows. Los datos están en
`.tools/mysql-local` dentro de este proyecto, y las credenciales aleatorias en `.env.local`
y archivos cliente ignorados. No publicar ni compartir esos archivos.
La aplicación usa únicamente el esquema `preciazo`; el usuario de pruebas, únicamente
`preciazo_shopping_test`. Los tests con create-drop se niegan a usar otro esquema MySQL.
No se tocan servidores del sistema ni se reescribe una configuración local existente.

```powershell
# Dentro de preciazo-web-services. RemoteSigned afecta solo a estos procesos.
powershell.exe -NoProfile -ExecutionPolicy RemoteSigned -File .\scripts\Start-Backend.ps1
# En otra terminal: contrato API contra el MySQL real aislado.
powershell.exe -NoProfile -ExecutionPolicy RemoteSigned -File .\scripts\Test-MySql.ps1
# Detener solo esta instancia local cuando ya no se necesite.
powershell.exe -NoProfile -ExecutionPolicy RemoteSigned -File .\scripts\Stop-LocalMySql.ps1
```

Para repetir esta preparación en otro Windows, coloca el [ZIP oficial de MySQL 8.4.13](https://dev.mysql.com/downloads/mysql/8.4.html?os=3)
extraído en `../.tools/mysql/mysql-8.4.13-winx64` y ejecuta Start-LocalMySql.ps1.
En otro servidor/Docker basta configurar las variables de desarrollo indicadas arriba y usar Maven;
los scripts portables son una comodidad local, no un requisito de despliegue.

El reinicio de esta instancia conservó datos confirmados en una tabla de verificación aislada,
retirada después de la comprobación. No se sembraron compradores o listas ficticias en el esquema de aplicación.

Verificación adicional del JAR con perfil dev y MySQL real: health UP, rutas Shopping en OpenAPI,
POST anónimo rechazado con 401 y tabla operacional sin listas de prueba. La instancia MySQL local
permanece disponible; el proceso Java temporal de validación se detuvo al terminar.
