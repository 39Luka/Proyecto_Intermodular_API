# ADR 0003: Rate Limiting Con Bucket4j (En Memoria, Una Sola Instancia)

## Contexto

Cualquier API publica puede ser abusada por accidente (clientes mal implementados, reintentos en bucle) o de forma intencional (fuerza bruta, scraping).
Queremos una capa de proteccion sencilla y de bajo mantenimiento para reducir carga y hacer el abuso mas costoso.

Este proyecto se despliega como una sola instancia de la API (sin requerimiento de escalado horizontal).

## Decision

- Aplicar rate limiting basico con **Bucket4j**.
- Guardar los buckets **en memoria** dentro del proceso.
- Usar como clave el usuario autenticado cuando exista; si no, usar la IP del cliente.
- Devolver HTTP `429` con `Retry-After` cuando se supera el limite.

## Por que

- Bucket4j es una libreria popular y probada (menos codigo propio).
- En memoria es lo mas simple para una sola instancia y mantiene pocas dependencias.
- Limitar por usuario evita penalizar a varios usuarios detras de la misma red; la IP cubre peticiones anonimas.

## Consecuencias

- Los limites se reinician al reiniciar la API y no se comparten entre replicas.
- Si en el futuro hay varias instancias, el rate limiting deberia moverse a un store compartido (por ejemplo Redis) o aplicarse en el gateway/balanceador.
