---
title: User Segmentation
type: concept
tags: [segmentation, user-profile, redis, elasticsearch]
created: 2026-04-20
updated: 2026-04-20
sources: [raw/articles/百万日活全端消息推送方案（Java云原生架构）.docx]
---

Grouping users so the dispatcher can pick the right channel and content for each one. Two axes: (1) *terminal-activity tier* — which channel reaches them right now, and (2) *profile-based cohorts* — who they are and what they've done.

## Terminal-activity tiers

Six tiers drive [[Multi-Channel Fallback]] ordering:

| Tier | Signal | Primary channel |
|---|---|---|
| Web active | WebSocket online | WebSocket → browser Notification → email (HP only) |
| Android active | App process alive (fg/bg) | Vendor (Mi / Huawei / OPPO / vivo) → Getui → SMS |
| iOS active | App process alive | APNs → JPush → SMS |
| Mini-program active | Open now or visited in last 24 h | WeChat subscribe → unified service → template msg |
| Multi-terminal active | ≥2 terminals active | User's preferred terminal; others silent-sync |
| Offline | No terminal reachable | Vendor system cache + mini-program deferred + SMS for HP |

Policy: use the highest-priority channel on the user's most-active terminal; suppress duplicates on other terminals.

## Profile cohorts

Three data types feed cohort definitions:

- **Static attributes** — gender, age, region, language, device type.
- **Dynamic behavior** — browsing, purchases, favorites, login frequency. Streamed via Kafka/RocketMQ; DB changes captured by Canal.
- **Preferences** — user-set push opt-ins, preferred terminal, quiet hours, plus inferred interest tags.

## Segment implementation

Two backends chosen by query shape:

- **Redis Bitmap** — for simple set membership (e.g., "users in Dalian" ∩ "active in last 7 days"). Bitwise AND/OR gets the target set in ≤100 ms.
- **Elasticsearch DSL** — for behavior-driven cohorts ("browsed electronics in last 30 days"). Richer queries, ≤500 ms.

## Related

- [[Multi-Channel Fallback]] — tier drives channel choice
- [[Personalization]] — cohorts + profile fill the template variables
- [[Push Notification Architecture]] — the segmentation layer sits in front of the push service
- [[Compliance]] — opt-in + retention constraints on profile data

## Source

- [Million-DAU Push Architecture](../sources/million-dau-push-architecture.md), §1.2 and §3.3
