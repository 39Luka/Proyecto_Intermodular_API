# ADR 0001: Access/Refresh Token Strategy

## Context

This API uses stateless authentication. Clients send an access token on each request.
Access tokens should be short-lived, but users also need a way to stay logged in without re-entering credentials.

We also want a server-side way to revoke sessions (logout) and reduce the impact of token leakage.

## Decision

- Use **JWT access tokens** for requests (short-lived).
- Use **opaque refresh tokens** for renewal via `POST /auth/refresh` (longer-lived).
- Store refresh tokens **hashed (SHA-256)** in DB, never raw.
- On refresh, **rotate**: revoke old token and issue a new one; on logout, revoke the token.

## Why

- JWTs are convenient for stateless request auth.
- Refresh tokens add server-side control (revocation/logout) without storing access tokens.
- Hashing + rotation reduces impact if a refresh token leaks.

## Consequences

- Requires a `refresh_tokens` table and related issue/rotate/revoke logic.
- Rotation means refresh tokens are single-use: once exchanged, the old one must stop working.

