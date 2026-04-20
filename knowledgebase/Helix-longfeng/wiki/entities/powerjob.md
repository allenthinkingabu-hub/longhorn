---
title: PowerJob
type: entity
tags: [scheduler, distributed-job, elastic-job, cron]
created: 2026-04-20
updated: 2026-04-20
sources: [raw/articles/百万日活全端消息推送方案（Java云原生架构）.docx]
---

Open-source distributed task scheduling framework for the JVM. In this architecture it handles real-time + CRON-style punctual jobs; Elastic-Job handles the daily broadcast pattern.

## Use in the article

- Real-time pushes: event arrives on RocketMQ, PowerJob dispatches a lightweight job — target ≤100 ms.
- Punctual pushes: CRON to the second; shard assignment via consistent hash on userId.
- Dynamic scale: executors can be added or removed with automatic shard rebalance.

## Complementary scheduler

Elastic-Job is used specifically for the **daily broadcast** pattern (every shard runs the same code against a different slice). Article treats the two as complementary rather than overlapping:

- PowerJob → event-driven and precise-time dispatch.
- Elastic-Job → sharded-broadcast recurring jobs.

## Why not one scheduler?

Article doesn't justify this explicitly. Plausible reasons:

- PowerJob's lightweight-job mode is tuned for short-lived fan-out, which matches event-driven pushes.
- Elastic-Job's sharded-broadcast mode simplifies the "8 a.m. daily digest to every user" pattern.
- A team already running both in-house may be avoiding migration cost.

## Related

- [[Task Scheduling]] — the concept page
- [[Push Notification Architecture]]
- [[Reliability]] — retry / dead-letter integration

## Source

- [Million-DAU Push Architecture](../sources/million-dau-push-architecture.md), §3.2
