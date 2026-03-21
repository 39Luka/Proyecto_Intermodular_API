# ADR 0003: Rate Limiting With Bucket4j (In-Memory, Single Instance)

## Context

Any public API can be abused accidentally (bad clients, retry storms) or intentionally (brute-force, scraping).
We want a small, low-maintenance protection layer to reduce load and make abuse more expensive.

This project is deployed as a single API instance (no horizontal scaling requirement).

## Decision

- Apply basic request rate limiting using **Bucket4j**.
- Store buckets **in-memory** in the API process.
- Key the limit by authenticated user when available, otherwise fall back to client IP.
- Return HTTP `429` with `Retry-After` when the limit is exceeded.

## Why

- Bucket4j is a popular, battle-tested token-bucket implementation (less custom code).
- In-memory is the simplest option for a single instance and keeps the dependency footprint small.
- Keying by user avoids punishing multiple users behind the same NAT; IP fallback keeps some protection for anonymous requests.

## Consequences

- Limits reset on API restart and are not shared across replicas.
- If the project later runs multiple instances, rate limiting should move to a shared store (e.g., Redis) or be enforced at the gateway/load balancer.

