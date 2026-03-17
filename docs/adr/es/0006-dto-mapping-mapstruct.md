# ADR 0006: Mapeo De DTOs Con MapStruct

## Contexto

Esta API expone DTOs de request/response y los mapea a entidades JPA y modelos de dominio.
El codigo de mapeo a mano suele crecer rapido, es repetitivo y con el tiempo es facil introducir errores sutiles.

Queremos reducir boilerplate manteniendo un mapeo explicito y seguro en compilacion.

## Decision

- Usar **MapStruct** para generar los mappers en build time.
- Mantener los mappers como beans de Spring (`componentModel = "spring"`).

## Por que

- MapStruct se usa mucho en proyectos Spring para mapear DTOs.
- Genera codigo Java normal (sin reflection en runtime), por lo que es rapido y depurable.
- Al generarse en compilacion, detecta antes campos faltantes o mapeos rotos.

## Consecuencias

- El build requiere annotation processing para MapStruct.
- Algunos mapeos complejos pueden necesitar metodos personalizados, pero en CRUD normal queda simple.
