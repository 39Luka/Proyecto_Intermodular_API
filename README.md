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

## Health (Actuator)

- `GET /actuator/health` is public (for uptime checks).
- `GET /actuator/prometheus` is public (for Prometheus scraping).
- Other `/actuator/**` endpoints require an `ADMIN` token.
- By default `health`, `info` and `prometheus` are exposed. Expand/reduce with `MANAGEMENT_ENDPOINTS` if needed.

## Dashboard (Business Metrics)

ADMIN-only endpoints intended for a client dashboard:

- `GET /dashboard/summary`: totals and revenue.
- `GET /dashboard/sales/daily?days=30`: paid purchases grouped by day for the last N days.

Using Swagger with JWT:

1. Call `POST /auth/login` and copy the `token`.
2. Click `Authorize` and paste the JWT (usually without the `Bearer` prefix).

Token lifecycle:

- `POST /auth/login` returns an access token (`token`) and a `refreshToken`.
- `POST /auth/refresh` rotates the refresh token and returns a new access token + refresh token.
- `POST /auth/logout` revokes a refresh token.

## Configuration (Environment Variables)

Configuration comes from `src/main/resources/application.properties` and `src/main/resources/application-prod.properties`.

- `PORT`: HTTP port (default `8080`)
- `DB_URL`: JDBC url. Example: `jdbc:mysql://localhost:3306/bakery_db?useSSL=false&serverTimezone=UTC`
- `DB_USERNAME`, `DB_PASSWORD`
- `JWT_SECRET`: JWT signing secret (required in prod)
- `JWT_EXPIRATION_MS`: access token lifetime (milliseconds). In prod the default is 15 minutes.
- `REFRESH_TOKEN_EXPIRATION_MS`: refresh token lifetime (milliseconds). Default is 30 days.
- `SHOW_SQL`: `true|false`
- `HIBERNATE_DDL_AUTO`: `update|validate|...` (prod default is `validate`)
- `spring.flyway.baseline-on-migrate`: enabled by default in this project to support existing schemas and empty DB bootstraps
- `CORS_ALLOWED_ORIGINS`: comma-separated. Example: `http://localhost:5173,https://your-frontend.com`
- `RATE_LIMIT_ENABLED`, `RATE_LIMIT_WINDOW_SECONDS`, `RATE_LIMIT_MAX_REQUESTS`: basic rate limiting (in-memory token bucket; applies to all endpoints by default)
- `RATE_LIMIT_EXCLUDED_PATH_PREFIXES`: comma-separated list of excluded path prefixes (optional). Example: `/swagger-ui,/v3/api-docs,/actuator/health,/actuator/info`
Admin bootstrap (only when there are no users in the DB and BOTH vars are set):

- `INITIAL_ADMIN_EMAIL`
- `INITIAL_ADMIN_PASSWORD`

Notes:

- `/auth/register` always creates a `USER`. Create admins via the bootstrap env vars above (only on empty DB) or via `/users/**` as an existing `ADMIN`.
- If you set only one of `INITIAL_ADMIN_EMAIL` or `INITIAL_ADMIN_PASSWORD`, the app fails fast at startup with a clear error.

## Auth & Permissions (Summary)

Send the token as `Authorization: Bearer <token>`.

- Public:
  - `POST /auth/login`
  - `POST /auth/register` (always creates a `USER`)
  - `GET /categories`, `GET /categories/{id}`
  - `GET /products`, `GET /products/{id}`, `GET /products/top-selling`
  - `GET /promotions/active`
  - Swagger (`/swagger-ui/**`, `/v3/api-docs/**`)
- Requires auth:
  - `GET /purchases`, `GET /purchases/{id}`, `POST /purchases`, `PATCH /purchases/{id}/pay|cancel`
- ADMIN only:
  - `POST|PUT|PATCH|DELETE` on `/categories/**`, `/products/**`, `/promotions/**`
  - `GET /promotions` and `GET /promotions/{id}`
  - `/users/**`

## Promotions

This project intentionally supports a single promotion type: percentage discounts.

- Create: `POST /promotions/percentage`
- Query active promotions for a product: `GET /promotions/active?productId=...`

## Enable/Disable (Soft Flags)

Instead of separate `/enable` and `/disable` endpoints, resources expose a single PATCH that toggles the flag:

- Product: `PATCH /products/{id}` with `{ "active": true|false }`
- Promotion: `PATCH /promotions/{id}` with `{ "active": true|false }`
- User: `PATCH /users/{id}` with `{ "enabled": true|false }`

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
- Integration tests against MySQL (Testcontainers) run when Docker is available.

## Architecture Decisions (ADR)

- `docs/adr/0001-jwt-access-refresh-tokens.md`
- `docs/adr/0002-jwt-validation-resource-server-hs256.md`
- `docs/adr/0003-rate-limiting-bucket4j.md`
- `docs/adr/0004-observability-actuator-prometheus.md`
- `docs/adr/0005-dto-mapping-mapstruct.md`
- `docs/adr/0006-initial-admin-bootstrap.md`
- `docs/adr/0007-error-format-problemdetail.md`
- `docs/adr/0008-configuration-properties-and-prod-checks.md`
- `docs/adr/0009-testing-h2-and-mysql-testcontainers.md`
- `docs/adr/0010-stock-concurrency-optimistic-locking.md`
- `docs/adr/0011-schema-management-flyway.md`

## Production (Railway)

Recommended:

1. `SPRING_PROFILES_ACTIVE=prod`
2. Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`.
3. On first deploy (empty DB), if you want admin bootstrap set `INITIAL_ADMIN_EMAIL` and `INITIAL_ADMIN_PASSWORD`.

Schema behavior in prod:

- Default is Flyway migrations + `HIBERNATE_DDL_AUTO=validate`.
- The app fails fast in `prod` if required env vars are missing (`DB_*`, `JWT_SECRET`).

## Concurrency Notes (Stock)

`products` use optimistic locking (`@Version`) so concurrent purchases cannot silently oversell inventory. If two requests try to update the same product stock at the same time, one may fail with `409 Concurrent update, please retry`.

## Credits

- Tutorial reference: https://www.youtube.com/watch?v=yluGdM1Wiow&t=641s
- Tutorial reference: https://www.youtube.com/watch?v=Cm8AOEiE0ZI&t=378s
- Tutorial reference: https://www.geeksforgeeks.org/advance-java/rate-limiting-a-spring-api-using-bucket4j/
