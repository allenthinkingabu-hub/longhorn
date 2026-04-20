---
title: Push Notification Architecture
type: concept
tags: [architecture, microservices, cloud-native]
created: 2026-04-20
updated: 2026-04-20
sources: [raw/articles/百万日活全端消息推送方案（Java云原生架构）.docx]
---

Reference four-layer microservice layout for a cross-platform push notification platform at ~1M DAU scale. Access → core service → channel → data, with RocketMQ as the async backbone and Kubernetes for elastic scaling.

## The four layers

1. **Access layer** — Nginx + OpenResty. Handles auth, schema validation, and rate limiting via Lua before hitting the core services. Sized for 百万级 QPS load balancing.
2. **Core service layer** — Spring Cloud Alibaba microservices, communicating via Dubbo. Four services, each independently scalable:
   - Template management — CRUD, versioning, i18n configuration
   - Task scheduling — time-strategy-based task firing + sharding
   - Message push — channel adaptation + per-channel fallback
   - Statistics / analytics — real-time counters + visualization + alarms
3. **Channel layer** — unified adapter that wraps webSocket, FCM, APNs, Mi/Huawei/OPPO/vivo vendor channels, Getui, JPush, WeChat subscribe message, etc. behind a common Java interface. Supports smart degradation + retry.
4. **Data layer** — MySQL (structured), Redis (session / token / offline cache / segments), Elasticsearch (logs + behavior data), ClickHouse (analytics at scale).

## Cross-cutting properties

| Property | Mechanisms |
|---|---|
| High availability | Nacos service registry, Sentinel circuit-breaking, Seata (AT mode) for distributed transactions |
| High concurrency | RocketMQ async fan-out, multi-level caching, Nginx+OpenResty LB, Netty Reactor threadpools |
| Extensibility | JSON Schema template definition, SPI channel plugins, k8s autoscaling |
| Observability | Prometheus+Grafana metrics, ELK logs, SkyWalking tracing |

## Why this shape?

- Separate *template* and *scheduling* from *sending* so template/version churn doesn't disrupt dispatch, and so sends can scale independently of CMS load.
- Putting [[Multi-Channel Fallback]] behind one adapter lets business code be channel-agnostic.
- Async backbone (RocketMQ) decouples real-time event ingest from delivery, absorbing spikes.

## Related

- [[Message Template]] — what flows through the template service
- [[Task Scheduling]] — how tasks are triggered
- [[Offline Push]] — what happens when the terminal isn't reachable
- [[Personalization]] — runs between template pull and channel call
- [[Observability]], [[Reliability]], [[Compliance]] — cross-cutting concerns

## Source

- [Million-DAU Push Architecture](../sources/million-dau-push-architecture.md), §2 "架构设计"
