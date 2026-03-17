# ADR 0007: Bootstrap De Admin Inicial Con Variables De Entorno (Solo DB Vacia)

## Contexto

En un despliegue nuevo, aun no existe una cuenta admin.
Necesitamos una forma segura de crear el primer administrador sin exponer un endpoint publico para "crear admin".

## Decision

- Crear el primer admin solo cuando la tabla de usuarios esta vacia.
- Requerir `INITIAL_ADMIN_EMAIL` y `INITIAL_ADMIN_PASSWORD`.
- Fallar rapido al arrancar si solo se define una de las dos variables.

## Por que

- Reduce superficie de ataque (sin endpoints especiales para elevar privilegios).
- Las variables de entorno son el mecanismo habitual para secretos de primer arranque.
- La regla de "solo DB vacia" evita cambios de privilegios accidentales en entornos ya existentes.

## Consecuencias

- Es una operacion de una sola vez: cuando ya hay usuarios, las variables se ignoran.
- La mala configuracion se detecta pronto (error al iniciar) y no queda en un estado inseguro silencioso.
