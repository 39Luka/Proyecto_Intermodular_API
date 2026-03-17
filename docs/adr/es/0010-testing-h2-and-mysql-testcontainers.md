# ADR 0010: Estrategia De Tests (H2 Por Defecto, MySQL Con Testcontainers)

## Contexto

Queremos tests rapidos pero con confianza de que la API funciona con la base de datos real (MySQL).
Solo H2 puede ocultar diferencias de dialecto y constraints.
Solo MySQL hace el ciclo de desarrollo mas lento y depende de setup local.

## Decision

- Ejecutar la mayoria de tests con BD en memoria (H2) por velocidad.
- Anadir tests de integracion opcionales contra MySQL real usando Testcontainers.
- Saltar automaticamente los tests de MySQL Testcontainers cuando Docker no esta disponible.

## Por que

- H2 mantiene el suite rapido y facil de ejecutar en cualquier entorno.
- MySQL Testcontainers detecta problemas reales de esquema/SQL sin exigir MySQL local configurado.
- Saltar sin Docker evita romper a contributors y entornos donde Docker no existe.

## Consecuencias

- Algunos problemas solo aparecen en los tests de integracion con MySQL; deberian ejecutarse en CI o antes de entregar.
- Para ejecutarlos en local hace falta Docker.

