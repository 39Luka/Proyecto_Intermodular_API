# ADR 0002: Validacion de JWT con Spring Security OAuth2 Resource Server (HS256)

## Contexto

La API autentica peticiones con JWT (access token) enviado como `Authorization: Bearer <token>`.
Queremos una forma estandar y probada de validar JWT (firma, expiracion, tokens malformados, manejo de errores) sin mantener un filtro propio.

Este proyecto usa un secreto simetrico (`jwt.secret`) con HMAC-SHA256 (HS256).

## Decision

- Usar **Spring Security OAuth2 Resource Server** para validar JWT entrantes.
- Usar **HS256** con un secreto compartido (`jwt.secret`) para firmar/validar.
- Mantener el claim `role` y mapearlo a authorities de Spring (`ROLE_ADMIN`, `ROLE_USER`) para que `hasRole(...)` siga funcionando.

## Por que

- Es la forma estandar de Spring Security para validar JWT (menos codigo custom).
- El resource server cubre validaciones comunes (firma, expiracion, tokens malformados) de forma consistente.
- HS256 es simple de operar para una sola instancia (solo hay que proteger `jwt.secret`).

## Consecuencias

- Menos codigo custom de seguridad.
- Un JWT sigue siendo valido hasta su expiracion (a menos que implementes revocacion de access tokens). El logout/revocacion se hace via refresh tokens.
- HS256 obliga a proteger muy bien el secreto. Si el proyecto crece a varios servicios, RSA puede ser mejor.

