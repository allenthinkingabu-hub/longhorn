---
title: Vendor Push Channels
type: entity
tags: [mobile, push, vendor, mipush, huawei, oppo, vivo, apns]
created: 2026-04-20
updated: 2026-04-20
sources: [raw/articles/百万日活全端消息推送方案（Java云原生架构）.docx]
---

Collective entity for the OS-level mobile push channels the architecture targets first before falling back to third-party SDKs. Each has its own credentials, limits, and quirks.

## The channels

| Vendor | Channel | Delivery (article claim) | Key constraints |
|---|---|---|---|
| Xiaomi | MiPush | 90%+ | Register on MiUI developer portal; AppID + AppKey; OS-level persistent connection — delivers even after app process is killed |
| Huawei | Push Kit | 99.9%+ | HMS developer cert; AppID + AppSecret; claimed 100 B+ msg/day, ≤100 ms |
| OPPO | OPPO Push | 99.99%+ | ColorOS reach ≥500 M devices |
| vivo | VPush | 99.9%+ | Throughput claim: 1 M msg/sec |
| Apple | APNs | 99%+ | Production + development cert split; stores at most **1** offline message per device |
| Google | FCM | — | Referenced in the channel adapter list but not detailed |

*Delivery numbers are vendor marketing figures; treat as upper bounds.*

## Why vendor-first

- Vendor channels use the OS push daemon, which survives when the app is force-stopped. Third-party channels usually cannot.
- Each vendor enforces its own channel — using the "wrong" channel on a given device can be silently throttled or dropped.
- Reach data from the article (e.g., OPPO's 500M-device ColorOS footprint) is the implicit justification for the vendor-first policy.

## Fallbacks

When a vendor channel is unavailable or returns an error, the adapter falls through to third-party push:

- **Getui (个推)** — Android fallback; filters users without notification permission; "link scheduling" tech to wake offline devices.
- **JPush (极光)** — iOS fallback.

See [[Multi-Channel Fallback]] for the full ladder.

## Related

- [[Multi-Channel Fallback]]
- [[Offline Push]] — vendor channels own mobile offline cache
- [[Push Notification Architecture]]

## Source

- [Million-DAU Push Architecture](../sources/million-dau-push-architecture.md), §4.2
