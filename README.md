# Bakery API

API REST (Spring Boot) para una panaderia: usuarios con roles, catalogo (categorias/productos), promociones y compras. La autenticacion es JWT.

## Stack

- Java 21 + Gradle
- Spring Boot 4 (WebMVC, Security, Validation, Data JPA)
- MySQL en produccion/desarrollo
- H2 en tests
- Swagger/OpenAPI (springdoc)

## Arranque rapido (local)

Prerequisitos: Java 21.

1. Configura variables de entorno (ejemplo en `.env.example`).
2. Arranca MySQL (local o remoto).
3. Ejecuta:

```powershell
./gradlew bootRun
```

La API queda en `http://localhost:8080` (o el puerto que definas en `PORT`).

## Swagger / OpenAPI

- UI: `http://localhost:8080/swagger-ui/index.html`
- JSON: `http://localhost:8080/v3/api-docs`

Autenticacion en Swagger:

1. Haz login en `POST /auth/login` y copia el `token`.
2. En Swagger pulsa `Authorize` y pega el JWT (normalmente sin el prefijo `Bearer`).

## Variables de entorno (principal)

La app lee la configuracion desde `src/main/resources/application.properties` y `src/main/resources/application-prod.properties`.

- `PORT`: puerto HTTP (default `8080`)
- `DB_URL`: JDBC url. Ejemplo: `jdbc:mysql://localhost:3306/bakery_db?useSSL=false&serverTimezone=UTC`
- `DB_USERNAME`, `DB_PASSWORD`
- `JWT_SECRET`: secreto para firmar JWT (en prod es obligatorio)
- `SHOW_SQL`: `true|false`
- `HIBERNATE_DDL_AUTO`: `update|validate|...` (en prod el default es `validate`)
- `CORS_ALLOWED_ORIGINS`: lista separada por comas. Ejemplo: `http://localhost:5173,https://tu-frontend.com`
- `app.seed.enabled`: `true|false` (por defecto `true`)

Bootstrap admin (solo si no hay ningun usuario en BD):

- `INITIAL_ADMIN_EMAIL`
- `INITIAL_ADMIN_PASSWORD`

## Seed de datos por defecto

En el arranque (excepto perfil `test`) se ejecuta `DefaultDataSeeder`:

- Si no hay usuarios y existen `INITIAL_ADMIN_EMAIL` / `INITIAL_ADMIN_PASSWORD`, crea el primer usuario `ADMIN`.
- Si `categories` y `products` estan vacias, inserta un catalogo basico (categorias/productos) para empezar rapido.
- Se puede desactivar con `app.seed.enabled=false`.

## Autenticacion y permisos (resumen)

El token se envia en `Authorization: Bearer <token>`.

- Publico:
  - `POST /auth/login`
  - `POST /auth/register` (crear `ADMIN` requiere ya ser `ADMIN`)
  - Swagger (`/swagger-ui/**`, `/v3/api-docs/**`)
- Requiere token:
  - `GET /categories`, `GET /categories/{id}`
  - `GET /products`, `GET /products/{id}`, `GET /products/top-selling`
  - `GET /purchases`, `GET /purchases/{id}`, `POST /purchases`, `PATCH /purchases/{id}/pay|cancel`
  - `GET /promotions/active`
- Solo `ADMIN`:
  - `POST|PUT|PATCH|DELETE` sobre `/categories/**`, `/products/**`, `/promotions/**`
  - `GET /promotions` y `GET /promotions/{id}`
  - `/users/**`

## Flujo tipico (manual con curl)

Login:

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'
```

Listar productos (con token):

```bash
curl http://localhost:8080/products \
  -H "Authorization: Bearer <token>"
```

## Tests

```powershell
./gradlew test
```

Notas:

- Los tests usan H2 en memoria (`src/test/resources/application.properties`).
- Gradle fuerza `spring.profiles.active=test` para evitar que CI use la configuracion de MySQL por defecto.

## Produccion (Railway)

Recomendado:

1. `SPRING_PROFILES_ACTIVE=prod`
2. Define `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`.
3. Si es primera vez (BD vacia) y quieres bootstrap del admin: define `INITIAL_ADMIN_EMAIL` y `INITIAL_ADMIN_PASSWORD`.

Si no usas migraciones aun y la BD esta vacia, temporalmente puedes setear `HIBERNATE_DDL_AUTO=update` para que cree tablas; luego vuelve a `validate`.

