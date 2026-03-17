# ADR 0011: Stock Concurrency With Optimistic Locking

## Context

Purchases update product stock. With concurrent requests, two purchases can read the same stock and both succeed unless concurrency is handled explicitly.
We need to avoid silent overselling.

## Decision

- Use JPA optimistic locking via `@Version` on `Product`.
- Translate optimistic locking conflicts into HTTP `409 Conflict` so clients can retry.

## Why

- Optimistic locking is simple and fits well when write conflicts are rare.
- It avoids holding long database locks while still preventing lost updates.

## Consequences

- In case of a conflict, one request fails and the client must retry.
- The API must document `409` as a possible response for purchase operations.

