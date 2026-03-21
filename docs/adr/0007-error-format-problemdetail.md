# ADR 0007: Error Response Format With ProblemDetail

## Context

Clients need a consistent error format (status, message, validation details) across the whole API.
Hand-rolled error DTOs tend to diverge as the project grows.

Spring Boot provides Problem Details (RFC 7807) as a standard way to represent HTTP API errors.

## Decision

- Use Spring's `ProblemDetail` as the canonical error response type.
- Keep a top-level `message` property for client simplicity and backward compatibility.
- Include a `timestamp` property for easier debugging.
- For validation errors, return a structured list of `{ field, message }` as the `message` payload.

## Why

- Problem Details is a standard format and integrates well with Spring exception handling.
- Reduces custom DTO surface area and keeps error behavior consistent.

## Consequences

- Error payloads are based on `ProblemDetail` fields plus custom properties.
- API documentation should describe the `message` property shape for each error type (string vs structured list).
