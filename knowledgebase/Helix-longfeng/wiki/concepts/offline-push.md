---
title: Offline Push
type: concept
tags: [offline, delivery, cache, vendor-channels]
created: 2026-04-20
updated: 2026-04-20
sources: [raw/articles/百万日活全端消息推送方案（Java云原生架构）.docx]
---

Per-terminal cache-and-replay strategy so users get messages sent while they were offline. Design split: the server only caches web messages; for mobile and mini-program, it leans on platform-level caches.

## Per-terminal strategy

| Terminal | Offline signal | Cache location | Replay |
|---|---|---|---|
| Web | WebSocket disconnect > 30 s | Server-side: Redis key `offline:web:{userId}`, TTL 7 days, cap 100 msgs/user | On WebSocket reconnect, server flushes cached unread |
| Android | Vendor returns "device offline" | Vendor system cache (Mi / Huawei / OPPO / vivo); Getui as fallback | Vendor pushes on device wake; Getui delivers on next app open |
| iOS | APNs returns "deviceToken invalid" or "device offline" | APNs stores last 1 message; JPush as fallback | APNs on wake; JPush on next app open |
| WeChat mini-program | Not opened for 24 h | WeChat server-side queue via `subscribeMessage.send`, ≤7 day TTL | Delivered on next mini-program open |

## Design choice: where caching lives

- **Web**: the server owns the full buffer because no platform-level offline store exists for a plain WebSocket.
- **Mobile**: vendor channels already cache at the OS level, so the server doesn't duplicate. It only owns *fallback* (Getui / JPush) for when vendor delivery fails.
- **Mini-program**: WeChat's subscribe-message API is itself a store-and-forward — the server only records the send intent.

This keeps the server-side cache small (web only), which is why one Redis key per user at 100 messages with a 7-day TTL is enough.

## Caveats

- iOS APNs only holds one message. If two messages queue while the device is offline, only the last one arrives. Apps that need reliable history must fetch on launch.
- WeChat's 7-day TTL is a hard ceiling. Beyond that, the message drops silently.

## Related

- [[Multi-Channel Fallback]] — channel degradation strategy
- [[Vendor Push Channels]] — the vendor-side caches
- [[Push Notification Architecture]]
- [[WeChat Subscribe Message]]

## Source

- [Million-DAU Push Architecture](../sources/million-dau-push-architecture.md), §3.5
