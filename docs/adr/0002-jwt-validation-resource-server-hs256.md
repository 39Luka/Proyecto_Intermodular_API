# ADR 0002: Validate JWTs With Spring Security OAuth2 Resource Server (HS256)

## Context

The API authenticates requests using JWT access tokens sent as `Authorization: Bearer <token>`.
We want a standard, well-tested validation pipeline (signature, expiration, malformed tokens, error handling) without maintaining a custom filter and parser.

This project currently uses a symmetric secret (`jwt.secret`) for HMAC-SHA256 (HS256).

## Decision

- Use **Spring Security OAuth2 Resource Server** to validate incoming JWTs.
- Use **HS256** with a shared secret (`jwt.secret`) for signing/verification.
- Keep the custom claim `role` and map it to Spring Security authorities (`ROLE_ADMIN`, `ROLE_USER`) so existing `hasRole(...)` rules keep working.

## Why

- This is the standard Spring Security way to validate JWTs (less custom code to maintain).
- The resource server handles common JWT validation concerns consistently (signature, expiry, malformed tokens).
- HS256 is simple to operate for a single-instance scope (just protect `jwt.secret`).

## Consequences

- Less custom security code and fewer security foot-guns.
- Tokens remain valid until they expire (unless you add server-side access token revocation). Logout/revocation is handled at the refresh-token layer.
- HS256 requires protecting the shared secret. If you later introduce multiple services or separate token issuer/validator, RSA becomes more attractive.

