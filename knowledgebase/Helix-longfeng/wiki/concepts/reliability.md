---
title: Reliability
type: concept
tags: [reliability, rate-limit, retry, disaster-recovery, multi-dc]
created: 2026-04-20
updated: 2026-04-20
sources: [raw/articles/百万日活全端消息推送方案（Java云原生架构）.docx]
---

Three layers — rate-limit/degrade, retry/idempotency, multi-DC failover — that together target 99.99% availability for the push platform.

## Rate limiting & degradation

- Per-interface rate limits via Sentinel, differentiated by priority: system announcements 10,000/s, marketing 5,000/s.
- Auto-downgrade per channel when error rate > 5% (falls through [[Multi-Channel Fallback]] hierarchy).
- Whole-system backpressure: pause low-priority traffic under load, keep core business running.
- Three-level degradation ladder (see [[Multi-Channel Fallback]]) controls recovery.

## Retry & idempotency

- Channel call: 3 retries with 1s / 2s / 4s exponential backoff on timeout or "retry" status codes.
- Task retry: failed tasks enter a dead-letter queue; configurable retry count (default 3); permanent failure is recorded with cause.
- Idempotency: every message has a unique ID; Redis `SETNX` on that ID prevents duplicate delivery.

## Disaster recovery

- **Active-standby across DCs.** Core services run in the Dalian DC; a standby cluster is deployed in Qingdao. Traffic cuts over via DNS round-robin. RPO ≤5 min, RTO ≤30 min.
- **Data durability**:
  - MySQL — primary/replica + daily full backup to OSS.
  - Redis — cluster mode with RDB + AOF.
  - Elasticsearch — cross-DC cluster with auto-replication.
- **Per-service HA** — ≥3 instances per service across availability zones; Nacos health checks auto-remove dead instances.

## Caveats

- RPO 5 min means up to 5 minutes of send-log / analytics loss on DC failure — acceptable for push analytics but noteworthy for compliance audits.
- "AT-mode" Seata distributed transactions carry their own perf cost; only template publish and task creation paths use them, not the hot send path.

## Related

- [[Multi-Channel Fallback]] — degradation mechanics
- [[Observability]] — what detects failures
- [[Task Scheduling]] — dead-letter queue
- [[Push Notification Architecture]]

## Source

- [Million-DAU Push Architecture](../sources/million-dau-push-architecture.md), §5.2
