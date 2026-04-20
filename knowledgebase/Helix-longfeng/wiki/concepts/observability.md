---
title: Observability
type: concept
tags: [observability, monitoring, metrics, logging, tracing]
created: 2026-04-20
updated: 2026-04-20
sources: [raw/articles/百万日活全端消息推送方案（Java云原生架构）.docx]
---

Three-pillar stack — metrics, logs, traces — deployed on-prem in Dalian with strict retention windows.

## Pillars

| Pillar | Tooling | Representative metrics / data | Retention |
|---|---|---|---|
| Metrics | Prometheus + Grafana | Send success / delivery / click / conversion; service QPS, response time, error rate; channel latency + availability; scheduling jitter | 30 days |
| Logs | Filebeat → Logstash → Elasticsearch → Kibana | Send logs (userId, templateId, channel, sendTime, status); service logs (req/resp, stack traces); channel return codes | 180 days |
| Traces | SkyWalking | Full E2E latency + per-hop breakdown (sharding, channel call, variable fill); error-link localization | 7 days |

## Where it's running

Prometheus is local to the Dalian datacenter to keep scrape latency ≤100 ms. Grafana dashboards are also local. Elasticsearch serves both logs (180 d) and traces (7 d) on the same cluster with different indices.

## What this buys

- **Latency budget enforcement.** SkyWalking shows where ≤200 ms budget is spent — variable fill vs. channel SDK vs. scheduling jitter.
- **Channel-level SLO tracking.** Per-channel availability and delivery drive the degradation thresholds in [[Multi-Channel Fallback]].
- **User-grain debugging.** A support ticket with a userId can pull the exact send path (template, channel chosen, vendor return code) from Kibana in seconds.

## Related

- [[Reliability]] — metrics feed the degradation ladder
- [[Push Notification Architecture]]

## Source

- [Million-DAU Push Architecture](../sources/million-dau-push-architecture.md), §5.1
