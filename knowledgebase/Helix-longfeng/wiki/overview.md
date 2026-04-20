---
title: Overview
type: overview
tags: [meta, synthesis]
created: 2026-04-20
updated: 2026-04-20
sources: [raw/articles/百万日活全端消息推送方案（Java云原生架构）.docx]
---

High-level synthesis across all sources in the wiki. Updated whenever a new ingested source shifts the big picture.

## Current focus

The wiki's domain, so far, is **large-scale cross-platform push notification systems** as implemented on a Chinese Java cloud-native stack. Single source ingested: an AI-generated reference architecture for a ~1 M DAU push platform covering web, Android, iOS, and WeChat mini-program.

## Themes

- **Template-driven dispatch.** Messages are JSON-Schema-shaped templates with i18n blocks, versioned and reviewable. See [[Message Template]].
- **Channel is the hardest problem.** The article spends most of its pages on *how to pick a channel* ([[Multi-Channel Fallback]]) and *what to do when it fails* ([[Offline Push]]). Everything else — templates, scheduling, personalization — is easier than channel orchestration.
- **Vendor-first mobile.** On Android the path starts at the OS push daemon (Mi / Huawei / OPPO / vivo) because that's the only channel that survives app-process-kill. See [[Vendor Push Channels]].
- **Reliability by convention.** 99.99% uptime is assumed; the question is whether the degradation ladder (see [[Reliability]]) and multi-DC active-standby actually deliver it. The article asserts rather than proves.
- **Compliance is a real architectural force.** WeChat's subscribe-message quotas, PIPL/GDPR opt-in tracking, and 广告法 "extreme words" filters shape the review pipeline more than engineering concerns do. See [[Compliance]].

## Map

```
            ┌─────────────────────────┐
            │    Message Template      │──→ uses → Internationalization
            │  (JSON Schema + i18n)    │──→ uses → Personalization
            └──────────┬───────────────┘
                       ▼
            ┌──────────────────────────┐
            │     Task Scheduling      │──→ powered by PowerJob + Elastic-Job
            │  real-time / punctual /  │
            │     daily broadcast      │
            └──────────┬───────────────┘
                       ▼
            ┌──────────────────────────┐
            │   User Segmentation      │──→ tiers + cohorts
            └──────────┬───────────────┘
                       ▼
            ┌──────────────────────────┐
            │  Multi-Channel Fallback  │──→ Vendor Push Channels (mobile)
            │                          │──→ Netty-socketio (web)
            │                          │──→ WeChat Subscribe Message
            │                          │──→ Offline Push on miss
            └──────────┬───────────────┘
                       ▼
        ┌──────────────┼──────────────┐
        ▼              ▼              ▼
   Observability   Reliability    Compliance
     (cross-cutting concerns)
```

## Open questions

- **Does the 50-ms personalization budget survive recommendation-engine calls?** Paper glosses over whether variable-fill is a cache lookup or a live model call.
- **What's the actual gray rollout protocol?** Template lifecycle says "10% → auto-abort on regression" but doesn't say what "regression" means quantitatively.
- **How does RocketMQ handle the 10 × send-fan-out at 100 K QPS?** Broker sizing, backpressure, and flow-control aren't discussed.
- **Can Getui / JPush actually wake a device?** Article's "link scheduling" claim needs verification against vendor docs.
- **Cross-DC sync under load.** Elasticsearch cross-DC replication is mentioned; consistency semantics under a brownout are not.

## What would shift the big picture

- A source on actual production incident post-mortems for a system at this scale.
- A vendor primary-source comparison (not marketing numbers).
- A security / privacy audit of the token / openId storage path.
- Alternate architectures (e.g., serverless push, event-mesh-based) for comparison.

Drop sources for any of the above into `raw/` and ask me to ingest.
