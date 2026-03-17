# ADR 0007: Initial Admin Bootstrap Via Environment Variables (Empty DB Only)

## Context

On a brand-new deployment, there is no admin account yet.
We need a safe way to create the first administrator without exposing a public "create admin" endpoint.

## Decision

- Bootstrap the first admin user only when the users table is empty.
- Require both `INITIAL_ADMIN_EMAIL` and `INITIAL_ADMIN_PASSWORD` to be set.
- Fail fast at startup if only one of the two variables is set.

## Why

- Keeps the API surface smaller (no special endpoint to elevate privileges).
- Environment variables are the standard way to provide first-run secrets in deployments.
- The "empty DB only" rule prevents accidental privilege changes in an existing environment.

## Consequences

- Admin bootstrap is a one-time operation: once users exist, env vars are ignored.
- Misconfiguration is surfaced early (startup error) to avoid silent insecure states.
