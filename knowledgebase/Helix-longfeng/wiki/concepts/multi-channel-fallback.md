---
title: Multi-Channel Fallback
type: concept
tags: [channels, degradation, retry, reliability]
created: 2026-04-20
updated: 2026-04-20
sources: [raw/articles/百万日活全端消息推送方案（Java云原生架构）.docx]
---

Ordered channel selection: start with the highest-reach, lowest-cost channel for a user's active terminal, and degrade to alternatives on error or error-rate thresholds.

## Channel hierarchy by terminal

- **Android active** → vendor (Mi / Huawei / OPPO / vivo) → Getui → SMS (emergency only).
- **iOS active** → APNs → JPush → SMS (emergency only).
- **Web active** → WebSocket → browser Notification → email (high-priority only).
- **Mini-program active** → subscribe message → unified service message → WeChat template msg (compliant contexts only).

SMS and email are reserved for high-priority messages (security alerts, etc.) because of cost and user annoyance.

## Failure detection & degradation

Three-level degradation ladder (from §5.2.1):

| Level | Trigger | Action | Recovery |
|---|---|---|---|
| 1 | Single-channel concurrency > 80% of threshold | Slow to 50% send rate | Auto-recover after 5 min below threshold |
| 2 | Multi-channel success rate < 70% | Pause low-priority traffic (marketing) | Manual + metrics green |
| 3 | System load ≥ 90% | Reject new requests with "busy" | Auto-recover at load ≤ 70% |

## Retry

- Per-channel retry: 3 attempts with 1s → 2s → 4s exponential backoff on timeout or "retry" status.
- Failed tasks land on a dead-letter queue with a configurable retry count (default 3). Past that, they are marked failed and recorded.
- Idempotency via Redis `SETNX` on a per-message ID — duplicate sends are suppressed.

## Unified adapter

A single Java interface covers every channel — [[Netty-socketio]] for web, FCM, APNs, Mi/Huawei/OPPO/vivo vendor SDKs, Getui, JPush, WeChat subscribe message — loaded via SPI so adding a channel is a plugin change, not a service-code change.

## Related

- [[User Segmentation]] — terminal-activity tier drives ordering
- [[Offline Push]] — what happens when every channel fails
- [[Reliability]] — the wider resilience strategy
- [[Vendor Push Channels]]
- [[Push Notification Architecture]]

## Source

- [Million-DAU Push Architecture](../sources/million-dau-push-architecture.md), §2.1 (channel layer), §4.2 (mobile), §5.2 (degradation)
