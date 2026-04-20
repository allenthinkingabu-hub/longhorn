---
title: Task Scheduling
type: concept
tags: [scheduling, powerjob, elastic-job, sharding]
created: 2026-04-20
updated: 2026-04-20
sources: [raw/articles/百万日活全端消息推送方案（Java云原生架构）.docx]
---

Three time strategies — real-time, punctual, daily — served by a hybrid of two schedulers, with user-ID-based consistent hashing for shard assignment.

## Time strategies

| Strategy | Trigger | Example | Latency target |
|---|---|---|---|
| **Real-time** | Business event arrives on RocketMQ | Order paid → confirm push | ≤100 ms |
| **Punctual** | Exact CRON time (to the second) | Meeting reminder 15 min before | On-time rate ≥99.9% |
| **Daily recurring** | Fixed cadence | 8 a.m. daily digest | Scheduled via shard broadcast |

## Scheduler split

- [[PowerJob]] handles real-time (lightweight job mode) and punctual (CRON) — it can dispatch at second-level precision and fan out shards.
- Elastic-Job handles daily broadcast jobs where every shard runs the same code against a different slice of users, with dynamic re-sharding on executor scale events.

## Execution flow

1. Client creates a job via the template service; jobId persisted to MySQL.
2. Scheduler shards the job by consistent hash over userId and hands each shard to an executor.
3. Executor consults [[User Segmentation]] to pick the optimal channel per user.
4. Executor calls the channel SDK and writes a send-log to Elasticsearch.
5. Channel callback updates MySQL and forwards to the analytics service.

## Why consistent-hash sharding?

- Minimizes re-assignment on executor scale events — only the newly-added / removed executors' users move.
- Avoids hot shards when users cluster by attribute (geography, time zone), assuming the hash key is a hashed userId.

## Related

- [[Push Notification Architecture]] — the scheduler is a dedicated microservice
- [[Reliability]] — retry + dead-letter queue for failed sends
- [[Multi-Channel Fallback]] — the "pick channel" step
- [[PowerJob]] — primary engine

## Source

- [Million-DAU Push Architecture](../sources/million-dau-push-architecture.md), §3.2
