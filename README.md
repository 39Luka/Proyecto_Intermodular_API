# Bakery API

REST API (Spring Boot) for a bakery: users/roles, catalog (categories/products), promotions and purchases. Authentication is JWT-based.

## Stack

- Java 21 + Gradle
- Spring Boot 4 (WebMVC, Security, Validation, Data JPA)
- Flyway (database migrations)
- MySQL for dev/production
- H2 for tests
- Swagger/OpenAPI (springdoc)

## Quick Start (Local)

Prerequisites: Java 21.

1. Configure environment variables (see `.env.example`).
2. Start MySQL (local or remote).
3. Run:

```powershell
./gradlew bootRun
```

API base URL: `http://localhost:8080` (or the port in `PORT`).

## Swagger / OpenAPI

- UI: `http://localhost:8080/swagger-ui/index.html`
- JSON: `http://localhost:8080/v3/api-docs`

Using Swagger with JWT:

1. Call `POST /auth/login` and copy the `token`.
2. Click `Authorize` and paste the JWT (usually without the `Bearer` prefix).

## Configuration (Environment Variables)

Configuration comes from `src/main/resources/application.properties` and `src/main/resources/application-prod.properties`.

- `PORT`: HTTP port (default `8080`)
- `DB_URL`: JDBC url. Example: `jdbc:mysql://localhost:3306/bakery_db?useSSL=false&serverTimezone=UTC`
- `DB_USERNAME`, `DB_PASSWORD`
- `JWT_SECRET`: JWT signing secret (required in prod)
- `SHOW_SQL`: `true|false`
- `HIBERNATE_DDL_AUTO`: `update|validate|...` (prod default is `validate`)
- `spring.flyway.baseline-on-migrate`: enabled by default in this project to support existing schemas and empty DB bootstraps
- `CORS_ALLOWED_ORIGINS`: comma-separated. Example: `http://localhost:5173,https://your-frontend.com`
- `app.seed.enabled`: `true|false` (default `true`)

Admin bootstrap (only when there are no users in the DB):

- `INITIAL_ADMIN_EMAIL`
- `INITIAL_ADMIN_PASSWORD`

## Default Data Seeding

On startup (except `test` profile) `DefaultDataSeeder` runs:

- If there are no users and `INITIAL_ADMIN_EMAIL` / `INITIAL_ADMIN_PASSWORD` are set, it creates the first `ADMIN`.
- If `categories` and `products` are empty, it inserts a small starter catalog to speed up setup.
- Disable with `app.seed.enabled=false`.

## Auth & Permissions (Summary)

Send the token as `Authorization: Bearer <token>`.

- Public:
  - `POST /auth/login`
  - `POST /auth/register` (creating an `ADMIN` requires being `ADMIN` already)
  - Swagger (`/swagger-ui/**`, `/v3/api-docs/**`)
- Requires auth:
  - `GET /categories`, `GET /categories/{id}`
  - `GET /products`, `GET /products/{id}`, `GET /products/top-selling`
  - `GET /purchases`, `GET /purchases/{id}`, `POST /purchases`, `PATCH /purchases/{id}/pay|cancel`
  - `GET /promotions/active`
- ADMIN only:
  - `POST|PUT|PATCH|DELETE` on `/categories/**`, `/products/**`, `/promotions/**`
  - `GET /promotions` and `GET /promotions/{id}`
  - `/users/**`

## Typical Flow (curl)

Login:

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'
```

List products (with token):

```bash
curl http://localhost:8080/products \
  -H "Authorization: Bearer <token>"
```

## Tests

```powershell
./gradlew test
```

Notes:

- Tests use in-memory H2 (`src/test/resources/application.properties`).
- Gradle forces `spring.profiles.active=test` so CI does not attempt to boot with MySQL settings.

## Production (Railway)

Recommended:

1. `SPRING_PROFILES_ACTIVE=prod`
2. Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`.
3. On first deploy (empty DB), if you want admin bootstrap set `INITIAL_ADMIN_EMAIL` and `INITIAL_ADMIN_PASSWORD`.

Schema behavior in prod:

- Default is Flyway migrations + `HIBERNATE_DDL_AUTO=validate`.
- The app fails fast in `prod` if required env vars are missing (`DB_*`, `JWT_SECRET`).
- If you must bootstrap using Hibernate temporarily: set `APP_ALLOW_UNSAFE_DDL_AUTO=true` together with `HIBERNATE_DDL_AUTO=update`, then remove both afterwards.

## Concurrency Notes (Stock)

`products` use optimistic locking (`@Version`) so concurrent purchases cannot silently oversell inventory. If two requests try to update the same product stock at the same time, one may fail with `409 Concurrent update, please retry`.
