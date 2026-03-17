# ADR 0009: Configuracion Tipada + Checks Fail-Fast En Produccion

## Contexto

La configuracion en Spring viene de muchas fuentes (properties, variables de entorno, secretos del deploy).
Cuando falta configuracion o es insegura, el peor caso es "arranca pero se comporta mal en produccion".

Queremos configuracion explicita, tipada y validada cuanto antes.

## Decision

- Usar records con `@ConfigurationProperties` para configuracion (JWT, CORS, rate limit, cache, datasource, JPA).
- En el perfil `prod`, ejecutar checks al inicio para:
  - exigir credenciales de BD y secreto JWT, y
  - rechazar configuraciones inseguras de auto-actualizacion de esquema (`ddl-auto=update|create|create-drop`).

## Por que

- La configuracion tipada reduce errores por strings y evita "magia" repartida por el codigo.
- Fallar rapido es mas seguro que arrancar con configuracion incompleta en produccion.
- Evita mutaciones accidentales del esquema en produccion; los cambios deben ir por migraciones.

## Consecuencias

- Una mala configuracion en produccion se convierte en error de arranque (a proposito).
- En `prod` hay que definir variables de entorno requeridas.

