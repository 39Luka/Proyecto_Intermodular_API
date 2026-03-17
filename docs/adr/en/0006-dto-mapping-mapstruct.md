# ADR 0006: DTO Mapping With MapStruct

## Context

This API exposes DTOs for requests/responses and maps them to JPA entities and domain models.
Hand-written mapping code tends to grow quickly, is repetitive, and is easy to get subtly wrong over time.

We want to reduce boilerplate while keeping mapping explicit and compile-time safe.

## Decision

- Use **MapStruct** to generate DTO mappers at build time.
- Keep mappers as Spring beans (`componentModel = "spring"`).

## Why

- MapStruct is widely used in Spring projects for DTO mapping.
- It generates plain Java code (no reflection at runtime), so it is fast and debuggable.
- Compile-time generation catches missing fields and broken mappings early.

## Consequences

- The build needs annotation processing for MapStruct.
- Some complex mappings may still require custom mapping methods, but most CRUD DTOs stay simple.

