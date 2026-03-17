# ADR 0004: Local Caching With Caffeine (Single Instance)

## Context

Most API traffic is read-heavy (listing categories/products, viewing details).
Repeated reads can hit the database unnecessarily and increase latency.

This project is intended to run as a single API instance, so a local cache is safe and effective.

## Decision

- Use Spring Cache with **Caffeine** as an in-memory cache provider.
- Cache only safe read models (read-only endpoints).
- Cache categories list and categories by-id.
- Cache active products list and active product by-id (user-visible view).
- Cache promotions admin list and promotions by-id.
- Cache users by-id and by-email (admin-only).
- Configure TTL and max size via environment (`CACHE_TTL`, `CACHE_MAX_SIZE`).

## Why

- Caffeine is a popular, high-performance local cache for JVM applications.
- Spring Cache keeps the implementation small (`@Cacheable` / `@CacheEvict`) and avoids custom caching code.
- Splitting caches for "active products" avoids leaking admin-only views via shared cache entries.

## Consequences

- Cache is per-instance and is lost on restart.
- Cache invalidation must be done on writes (create/update/delete) to avoid stale reads.
- If the deployment becomes multi-instance, consider a distributed cache (Redis) or accept eventual consistency per node.
