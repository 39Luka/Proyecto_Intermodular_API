# ADR 0004: Cache Local Con Caffeine (Una Sola Instancia)

## Contexto

Muchas APIs reciben mas lecturas que escrituras (listar categorias/productos, ver detalles).
Si repetimos lecturas sin cache, aumentamos carga en la base de datos y latencia.

Este proyecto se ejecuta como una sola instancia, por lo que un cache local es seguro y efectivo.

## Decision

- Usar Spring Cache con **Caffeine** como cache en memoria.
- Cachear solo lecturas seguras (endpoints de lectura).
- Cachear lista de categorias y categorias por id.
- Cachear lista de productos activos y producto activo por id (vista de usuario).
- Cachear lista de promociones (admin) y promociones por id.
- Cachear usuarios por id y por email (solo admin).
- Configurar TTL y tamano maximo por entorno (`CACHE_TTL`, `CACHE_MAX_SIZE`).

## Por que

- Caffeine es un cache local popular y de alto rendimiento en la JVM.
- Spring Cache reduce el codigo propio (`@Cacheable` / `@CacheEvict`).
- Separar caches de "productos activos" evita filtrar vistas de admin a usuarios por entradas compartidas.

## Consecuencias

- El cache es por instancia y se pierde al reiniciar.
- Hay que invalidar al escribir (crear/actualizar/borrar) para evitar lecturas obsoletas.
- Si en el futuro hay varias instancias, considerar cache distribuido (Redis) o aceptar consistencia eventual por nodo.
