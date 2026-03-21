# ADR 0009: Testing Strategy (H2 by Default, MySQL with Testcontainers)

## Context

We want fast feedback from tests while still having confidence that the API works with the real database engine (MySQL).
Relying only on H2 can hide dialect and constraint differences.
Relying only on MySQL slows down the inner loop and makes tests dependent on local setup.

## Decision

- Run most tests with an in-memory database (H2) for speed.
- Add optional integration tests that run against real MySQL using Testcontainers.
- Auto-skip MySQL Testcontainers tests when Docker is not available.

## Why

- H2 keeps the test suite fast and easy to run anywhere.
- MySQL Testcontainers catches real-world schema/SQL issues without requiring manual local MySQL setup.
- Skipping when Docker is missing avoids breaking contributors and CI environments that do not provide Docker.

## Consequences

- Some issues are only detected in MySQL integration tests; they should run at least in CI or before releases.
- Developers need Docker to run the full integration test suite locally.
