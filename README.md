# Bakery API

REST API (Spring Boot) for a bakery: users/roles, catalog (categories/products), promotions and purchases. Authentication is JWT-based.

## 🆕 Recent Improvements (April 2026)

**P1 (Critical) - Completed:**
- ✅ Comprehensive logging (SLF4J + Logback) in all services
- ✅ JWT Refresh Tokens (7 days expiration) with new `/auth/refresh` endpoint
- ✅ Controller tests for Categories and Promotions

**P2 (High) - Completed:**
- ✅ Actuator health checks & Prometheus metrics
- ✅ Image validation (size 5MB max, MIME type checking)
- ✅ Rate limiting configuration (Bucket4J)

**P3+ - In Progress:** See `PRODUCTION_GUIDE.md`

## Stack

- Java 21 + Gradle
- Spring Boot 4 (WebMVC, Security, Validation, Data JPA)
- MySQL for dev/production
- H2 for tests
- Swagger/OpenAPI (springdoc)
- Logging: SLF4J + Logback
- Metrics: Micrometer + Prometheus
- Rate Limiting: Bucket4J

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

### Core Settings
- `PORT`: HTTP port (default `8080`)
- `SPRING_PROFILES_ACTIVE`: `dev` or `prod`

### Database
- `DB_URL`: JDBC url. Example: `jdbc:mysql://localhost:3306/bakery_db?useSSL=false&serverTimezone=UTC`
- `DB_USERNAME`, `DB_PASSWORD`
- `HIBERNATE_DDL_AUTO`: `update` (dev) or `validate` (prod)

### JWT & Authentication
- `JWT_SECRET`: JWT signing secret (required in prod, min 32 chars)
- `JWT_EXPIRATION_MS`: Access token lifetime in milliseconds (default: `900000` = 15 min)
- `JWT_REFRESH_EXPIRATION_MS`: Refresh token lifetime (default: `604800000` = 7 days)

### Logging
- `LOG_FILE`: Path to log file (default: `logs/bakery-api.log`)
- `SHOW_SQL`: `true|false` (show Hibernate SQL)

### CORS
- `CORS_ALLOWED_ORIGINS`: Comma-separated. Example: `http://localhost:5173,https://your-frontend.com`

### Actuator & Monitoring
- `OPENAPI_ENABLED`: `true|false` (Swagger UI enabled, default true for dev, false for prod)
- `RATE_LIMIT_REQUESTS_PER_MINUTE`: Requests per minute per IP (default: `100`)

### Memory & Connection Pool
- `LAZY_INIT`: `true|false` (lazy bean initialization)
- `DB_POOL_MAX`: Max pool size (default: `10`)
- `DB_POOL_MIN_IDLE`: Min idle connections (default: `0`)
- `PAGINATION_MAX_PAGE_SIZE`: Max page size for API (default: `100`)

## Auth & Permissions (Summary)

Send the token as `Authorization: Bearer <token>`.

- Public:
  - `POST /auth/login` - Returns access token + refresh token
  - `POST /auth/refresh` - Refresh access token using refresh token
  - `POST /auth/register` (always creates a `USER`)
  - `GET /categories`, `GET /categories/{id}`
  - `GET /products`, `GET /products/{id}`, `GET /products/top-selling`
  - `GET /promotions/active`
  - `GET /actuator/health` - Health check (public)
  - Swagger (`/swagger-ui/**`, `/v3/api-docs/**`)
- Requires auth:
  - `GET /purchases`, `GET /purchases/{id}`, `POST /purchases`, `PATCH /purchases/{id}/pay|cancel`
  - `GET /actuator/metrics` - Metrics (ADMIN)
  - `GET /actuator/prometheus` - Prometheus metrics (ADMIN)
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
- User: `PATCH /users/{id}` with `{ "active": true|false }`

## Typical Flow (curl)

Login and get tokens:

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'

# Response: { "token": "eyJ...", "refreshToken": "eyJ...", "expiresIn": 900000 }
```

Refresh access token when it expires:

```bash
curl -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"eyJ..."}'

# Response: { "token": "eyJ...", "refreshToken": "eyJ...", "expiresIn": 900000 }
```

List products (with token):

```bash
curl http://localhost:8080/products \
  -H "Authorization: Bearer <token>"
```

Create purchase (USER: omit userId; ADMIN: include userId):

```bash
curl -X POST http://localhost:8080/purchases \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"items":[{"productId":10,"quantity":2,"promotionId":null}]}'
```

Check health:

```bash
curl http://localhost:8080/actuator/health
```

Get metrics (requires ADMIN role):

```bash
curl -H "Authorization: Bearer <admin-token>" \
  http://localhost:8080/actuator/prometheus
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

Schema behavior in prod:

- Default is `HIBERNATE_DDL_AUTO=validate` (recommended).

## Concurrency Notes (Stock)

`products` use optimistic locking (`@Version`) so concurrent purchases cannot silently oversell inventory. If two requests try to update the same product stock at the same time, one may fail with `409 Concurrent update, please retry`.

## Credits

- Tutorial reference: https://www.youtube.com/watch?v=yluGdM1Wiow&t=641s
- Tutorial reference: https://www.youtube.com/watch?v=Cm8AOEiE0ZI&t=378s
