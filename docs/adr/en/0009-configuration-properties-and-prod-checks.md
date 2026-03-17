# ADR 0009: Typed Configuration + Production Fail-Fast Checks

## Context

Spring configuration comes from many sources (properties files, environment variables, deploy secrets).
When configuration is missing or unsafe, the worst case is "it boots but behaves incorrectly in production".

We want configuration to be explicit, typed, and validated early.

## Decision

- Use `@ConfigurationProperties` records for application configuration (JWT, CORS, rate limit, cache, datasource, JPA).
- In the `prod` profile, run startup checks to:
  - require DB credentials and JWT secret, and
  - reject unsafe schema auto-update settings (`ddl-auto=update|create|create-drop`).

## Why

- Typed configuration reduces stringly-typed mistakes and spreads less magic across the codebase.
- Failing fast is safer than running with partial config in production.
- Prevents accidental schema mutations in production; schema changes should go through migrations.

## Consequences

- Production misconfiguration becomes a startup error (intentional).
- Developers need to set required env vars when running with `SPRING_PROFILES_ACTIVE=prod`.

