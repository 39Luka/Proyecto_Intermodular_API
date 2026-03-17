# ADR 0012: Gestion De Esquema Con Flyway

## Contexto

Los cambios de esquema deben ser repetibles y predecibles en todos los entornos.
Depender de Hibernate `ddl-auto=update` es comodo en desarrollo pero arriesgado: puede cambiar el esquema de forma inesperada y generar drift.

Queremos una unica fuente de verdad para la evolucion del esquema.

## Decision

- Usar migraciones Flyway como fuente de verdad de los cambios de esquema.
- En produccion, mantener Hibernate en `validate` para fallar rapido ante diferencias en vez de mutar el esquema.
- Mantener `baseline-on-migrate` activado para soportar bootstrap en DB vacia y esquemas existentes.

## Por que

- Flyway da migraciones explicitas y versionadas, faciles de revisar y reproducir.
- `validate` evita cambios accidentales en produccion.
- `baseline-on-migrate` facilita la adopcion inicial cuando la BD ya puede existir.

## Consecuencias

- Cada cambio de esquema requiere un fichero de migracion.
- En local se deben ejecutar migraciones (o confiar en el arranque de la app) para mantener la BD sincronizada.

