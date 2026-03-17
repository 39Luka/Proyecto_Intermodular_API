# ADR 0012: Database Schema Management With Flyway

## Context

Schema changes must be repeatable and predictable across environments.
Relying on Hibernate `ddl-auto=update` is convenient in development but risky: it can produce unexpected schema changes and drift.

We want a single source of truth for schema evolution.

## Decision

- Use Flyway migrations as the source of truth for database schema changes.
- In production, keep Hibernate DDL mode at `validate` so schema mismatches fail fast instead of mutating the schema.
- Keep `baseline-on-migrate` enabled to support empty DB bootstraps and existing schemas.

## Why

- Flyway provides explicit, versioned migrations that are easy to review and reproduce.
- `validate` prevents accidental schema changes in production.
- `baseline-on-migrate` smooths initial adoption when the DB might already exist.

## Consequences

- Every schema change requires a migration file.
- Developers should run migrations locally (or rely on app startup) to keep DB in sync.

