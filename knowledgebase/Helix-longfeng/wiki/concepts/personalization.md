---
title: Personalization
type: concept
tags: [personalization, user-profile, variable-fill, recommendations]
created: 2026-04-20
updated: 2026-04-20
sources: [raw/articles/百万日活全端消息推送方案（Java云原生架构）.docx]
---

Turning a shared template into a user-specific message at send time. Runs on the output of [[User Segmentation]] and feeds the channel adapter.

## Three personalization kinds

- **Variable fill** — template has placeholders like `{userName}`, `{productName}`; they're filled from the user profile at dispatch. Target ≤50 ms per message.
- **Locale switch** — per-language content blocks in the template are selected via [[Internationalization]] logic.
- **Content injection** — recommendation engine supplies content for slots like `{recommendContent}` (product list, article title), so the same template can deliver different copy to different users.

## Data sources

Three profile buckets:

- **Static** — gender, age, region, language, device type. From signup form + IP inference.
- **Dynamic** — browsing, purchases, favorites, login frequency. Streamed via Kafka/RocketMQ; DB-side changes captured by Canal binlog → sync.
- **Preferences** — opt-ins, preferred terminal, time windows; also inferred interest tags.

## Where it runs

In the message push service, between "template resolved" and "channel SDK called":

1. Pull template + user profile.
2. Pick locale.
3. Fill variables (including recommendation calls if needed).
4. Hand off to [[Multi-Channel Fallback]] for dispatch.

## Caveats

- The 50-ms target is tight — treat recommendation-engine calls as cached lookups, not on-path model inference.
- Profile freshness depends on Canal sync lag. Stale profiles produce stale personalization; compliance teams also need to verify that opt-outs propagate quickly.

## Related

- [[Message Template]] — where placeholders live
- [[User Segmentation]] — who's in scope for a given campaign
- [[Internationalization]] — locale resolution
- [[Compliance]] — consent for behavioral data

## Source

- [Million-DAU Push Architecture](../sources/million-dau-push-architecture.md), §3.3
