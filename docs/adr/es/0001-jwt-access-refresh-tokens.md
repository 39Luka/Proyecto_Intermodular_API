# ADR 0001: Estrategia de Access/Refresh Tokens

## Contexto

Esta API usa autenticacion sin estado. El cliente envia un access token en cada peticion.
Los access tokens deben ser de corta duracion, pero el usuario necesita poder mantener sesion sin volver a introducir credenciales.

Tambien queremos una forma en el servidor de revocar sesiones (logout) y reducir el impacto si un token se filtra.

## Decision

- Usar **JWT access tokens** para autenticar peticiones (corta duracion).
- Usar **refresh tokens opacos** (cadena aleatoria) para renovar via `POST /auth/refresh` (mas larga duracion).
- Guardar en BD solo el **hash (SHA-256)** del refresh token, nunca el valor en claro.
- En refresh, **rotar**: revocar el token antiguo y emitir uno nuevo; en logout, revocar el token.

## Por que

- JWT es comodo para autenticacion stateless.
- Refresh tokens aportan control en servidor (revocacion/logout) sin guardar access tokens.
- Hash + rotacion reduce el impacto si un refresh token se filtra.

## Consecuencias

- Requiere tabla `refresh_tokens` y logica de emitir/rotar/revocar.
- La rotacion hace que el refresh token sea de un solo uso: al canjearlo, el anterior debe dejar de funcionar.

