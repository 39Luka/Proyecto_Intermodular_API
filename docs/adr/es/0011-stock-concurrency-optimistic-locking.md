# ADR 0011: Concurrencia De Stock Con Optimistic Locking

## Contexto

Las compras actualizan el stock del producto. Con requests concurrentes, dos compras pueden leer el mismo stock y ambas "ganar" si no se gestiona la concurrencia.
Necesitamos evitar vender mas unidades de las disponibles sin darnos cuenta.

## Decision

- Usar optimistic locking de JPA con `@Version` en `Product`.
- Convertir conflictos de optimistic locking a HTTP `409 Conflict` para que el cliente pueda reintentar.

## Por que

- Optimistic locking es simple y encaja bien cuando los conflictos de escritura son poco frecuentes.
- Evita locks largos en base de datos y aun asi previene lost updates.

## Consecuencias

- En caso de conflicto, una request falla y el cliente debe reintentar.
- La API debe documentar `409` como respuesta posible en operaciones de compra.

