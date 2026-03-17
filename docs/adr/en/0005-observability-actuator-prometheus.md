# ADR 0005: Observability With Actuator + Prometheus Metrics

## Context

For production-like environments, we need visibility into the API (uptime, performance, error rates).
Storing operational metrics (like counters) directly in application tables is usually the wrong tool: it mixes concerns and adds write load to the main database.

We want a standard way to export metrics so an external system can scrape, store and chart them.

## Decision

- Use **Spring Boot Actuator** for health/info endpoints.
- Use **Micrometer Prometheus registry** to expose metrics at `GET /actuator/prometheus`.
- Keep Prometheus/Grafana as external infrastructure (not part of this repository).

## Why

- Actuator + Micrometer is the standard Spring Boot approach for operational metrics.
- Prometheus scraping avoids extra writes in the application and keeps metric storage/retention concerns outside the API.
- This matches common industry setups and is easy to extend later (timers, custom counters, JVM metrics).

## Consequences

- Metrics are pulled (scraped) by Prometheus; the API must allow access to `/actuator/prometheus`.
- The project needs external monitoring components to get dashboards/alerts (Prometheus, Grafana, Alertmanager).

