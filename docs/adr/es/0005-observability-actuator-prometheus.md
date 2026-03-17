# ADR 0005: Observabilidad Con Actuator + Metricas Prometheus

## Contexto

En entornos reales necesitamos visibilidad de la API (salud, rendimiento, tasas de error).
Guardar metricas operacionales (por ejemplo contadores) directamente en tablas de la aplicacion normalmente no es buena idea: mezcla responsabilidades y mete carga de escritura en la base de datos principal.

Queremos una forma estandar de exportar metricas para que un sistema externo las recoja, almacene y grafique.

## Decision

- Usar **Spring Boot Actuator** para endpoints de health/info.
- Usar **Micrometer Prometheus registry** para exponer metricas en `GET /actuator/prometheus`.
- Mantener Prometheus/Grafana como infraestructura externa (fuera de este repo).

## Por que

- Actuator + Micrometer es el enfoque estandar en Spring Boot para metricas operacionales.
- El scraping de Prometheus evita escrituras extra en la aplicacion y saca el almacenamiento/retencion fuera de la API.
- Es comun en empresas y es facil de ampliar (timers, counters, metricas JVM).

## Consecuencias

- Las metricas se recogen por scraping; la API debe permitir acceso a `/actuator/prometheus`.
- Para dashboards/alertas hacen falta componentes externos (Prometheus, Grafana, Alertmanager).
