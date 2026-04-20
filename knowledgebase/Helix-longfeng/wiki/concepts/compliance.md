---
title: Compliance
type: concept
tags: [compliance, privacy, consent, regulation, pipl, gdpr]
created: 2026-04-20
updated: 2026-04-20
sources: [raw/articles/百万日活全端消息推送方案（Java云原生架构）.docx]
---

Three-axis control — user authorization, content review, data privacy — mapped to PRC and international regulations. Violations mean platform bans, not just fines.

## User authorization

- All push traffic requires explicit opt-in (WeChat subscribe modal, APP notification permission).
- One-click unsubscribe must be reachable from the terminal's settings.
- Authorization + unsubscribe events logged for 180 days.
- Basis: 《个人信息保护法》(PIPL), 《微信小程序订阅消息规范》.

## Content review

- Template review is two-stage: automated JSON-Schema + keyword filter, then human review for ad/legal compliance.
- "Extreme words" (e.g., "最优惠", "国家级") are blocked to comply with 广告法.
- Real-time content check at send time via third-party moderation (e.g., Baidu content safety) for personalization payloads.
- Basis: 《广告法》, 《网络安全法》.

## Data privacy

- Device token / openId stored AES-256 encrypted.
- userId hashed in send logs — no plaintext IDs at rest.
- Cross-border transfer requires explicit consent and happens only when necessary.
- Basis: PIPL, GDPR.

## Platform-specific constraints

WeChat mini-program in particular has hard frequency caps (see [[WeChat Subscribe Message]]):

- One-time subscribe: up to 2 messages per subscription, valid 1 year; requires user-triggered authorization UI.
- Long-term subscribe: 8 messages/month, limited to government / medical / transit / finance / education verticals.
- Per-user rate: 1 msg/sec; daily cap 3M (payment-enabled) or 1M (not).

## Related

- [[Message Template]] — the review gate
- [[Personalization]] — data minimization constraints
- [[User Segmentation]] — consent scope limits cohort reach
- [[WeChat Subscribe Message]]

## Source

- [Million-DAU Push Architecture](../sources/million-dau-push-architecture.md), §4.3.1 and §5.3
