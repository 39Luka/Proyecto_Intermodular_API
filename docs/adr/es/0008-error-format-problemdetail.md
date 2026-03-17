# ADR 0008: Formato De Errores Con ProblemDetail

## Contexto

Los clientes necesitan un formato de error consistente (status, mensaje, detalles de validacion) en toda la API.
Los DTOs de error hechos a mano suelen divergir segun crece el proyecto.

Spring Boot ofrece Problem Details (RFC 7807) como formato estandar para representar errores HTTP en APIs.

## Decision

- Usar `ProblemDetail` como tipo canonico de respuesta de error.
- Mantener una propiedad superior `message` por simplicidad del cliente y compatibilidad.
- Incluir una propiedad `timestamp` para facilitar el debugging.
- Para errores de validacion, devolver una lista estructurada de `{ field, message }` como payload de `message`.

## Por que

- Problem Details es un estandar y encaja bien con el manejo de excepciones de Spring.
- Reduce codigo propio y mantiene consistencia.

## Consecuencias

- El payload de error se basa en campos de `ProblemDetail` mas propiedades custom.
- La documentacion de la API debe describir la forma de `message` (string vs lista estructurada) segun el caso.

