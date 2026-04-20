---
title: WeChat Subscribe Message
type: entity
tags: [wechat, mini-program, subscribe-message, api]
created: 2026-04-20
updated: 2026-04-20
sources: [raw/articles/百万日活全端消息推送方案（Java云原生架构）.docx]
---

WeChat's opt-in messaging channel for mini-programs. The only compliant path from a server to a user's WeChat client when they are not actively in the mini-program.

## Two modes

- **One-time subscribe** (`wx.requestSubscribeMessage`) — any mini-program can request this. User's permission grants the right to send **2** messages per template subscription, valid for 1 year.
- **Long-term subscribe** — only for specific verticals (government, medical, transportation, finance, education). Once granted, up to **8 messages/month**.

## Authorization UX rules

- Must be triggered by a user action (button tap, form submit). Auto-prompts on app load are forbidden.
- The prompt lists one or more template IDs; user accepts/declines per template.
- Each template ID requires WeChat review.

## Rate limits

- Per user per mini-program: 1 msg/sec.
- Daily: 3 M messages for payment-enabled mini-programs, 1 M for others.

## Send path (from §4.3.2 article code sample)

1. Mini-program client calls `wx.requestSubscribeMessage` with up to 3 template IDs; on `accept`, reports to server.
2. Server obtains an access_token via the appId/appSecret credentials.
3. Server POSTs to `https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=...` with `touser` (openId), `template_id`, `page` (deep link), `data` (template variables).

## Offline behavior

WeChat's server queues the message if the user is offline; delivered on next mini-program open. 7-day TTL — beyond that, the message silently drops. See [[Offline Push]].

## Fallback

If subscribe message is unavailable or ineligible, the architecture uses:

1. Unified service message (统一服务消息).
2. WeChat template message (legacy, compliance-restricted).

## Related

- [[Offline Push]] — mini-program side
- [[Multi-Channel Fallback]]
- [[Compliance]] — the tightest platform constraint in the system
- [[Push Notification Architecture]]

## Source

- [Million-DAU Push Architecture](../sources/million-dau-push-architecture.md), §4.3
