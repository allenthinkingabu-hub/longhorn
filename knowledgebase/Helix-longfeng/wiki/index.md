---
title: Wiki Index
type: overview
tags: [meta, index]
created: 2026-04-20
updated: 2026-04-20
sources: []
---

Catalog of every page in the wiki. Updated on every INGEST / QUERY / LINT.

## Overview

- [Overview](overview.md) — high-level synthesis across all sources (1 source, 2026-04-20)
- [Schema](schema.md) — wiki conventions and page format (0 sources, 2026-04-20)
- [Log](log.md) — chronological record of all wiki operations (0 sources, 2026-04-20)

## Concepts

- [Push Notification Architecture](concepts/push-notification-architecture.md) — four-layer microservice layout for ~1M-DAU push (1 source, 2026-04-20)
- [Message Template](concepts/message-template.md) — JSON-Schema templates with i18n blocks and a five-state lifecycle (1 source, 2026-04-20)
- [User Segmentation](concepts/user-segmentation.md) — terminal-activity tiers + profile cohorts for targeting (1 source, 2026-04-20)
- [Task Scheduling](concepts/task-scheduling.md) — real-time / punctual / daily strategies on PowerJob + Elastic-Job (1 source, 2026-04-20)
- [Offline Push](concepts/offline-push.md) — per-terminal cache-and-replay across web / mobile / mini-program (1 source, 2026-04-20)
- [Multi-Channel Fallback](concepts/multi-channel-fallback.md) — priority-ordered channel selection with degradation ladder (1 source, 2026-04-20)
- [Personalization](concepts/personalization.md) — variable fill, locale switch, content injection (1 source, 2026-04-20)
- [Internationalization](concepts/internationalization.md) — locale selection via three-tier fallback (1 source, 2026-04-20)
- [Observability](concepts/observability.md) — metrics + logs + traces with on-prem retention (1 source, 2026-04-20)
- [Reliability](concepts/reliability.md) — rate-limit, retry/idempotency, multi-DC failover (1 source, 2026-04-20)
- [Compliance](concepts/compliance.md) — user consent, content review, data privacy (1 source, 2026-04-20)

## Entities

- [Netty-socketio](entities/netty-socketio.md) — Socket.IO server on Netty, web push backbone (1 source, 2026-04-20)
- [PowerJob](entities/powerjob.md) — distributed scheduler for real-time + CRON jobs (1 source, 2026-04-20)
- [Vendor Push Channels](entities/vendor-push-channels.md) — Mi / Huawei / OPPO / vivo / APNs / FCM collective (1 source, 2026-04-20)
- [WeChat Subscribe Message](entities/wechat-subscribe-message.md) — WeChat mini-program opt-in messaging API (1 source, 2026-04-20)
- [Doubao AI](entities/doubao-ai.md) — ByteDance LLM; author of the source article (1 source, 2026-04-20)

## Sources

- [Million-DAU Push Architecture](sources/million-dau-push-architecture.md) — AI-generated Chinese-language architecture proposal for cross-platform push at ~1M DAU (1 source, 2026-04-20)

## Queries

- [Implementation Plan](queries/implementation-plan.md) — executable rollout plan for the push platform, 7 phases over ~13-15 weeks, 346 person-days (1 source, 2026-04-20)
