---
title: Internationalization
type: concept
tags: [i18n, locale, spring, utf-8]
created: 2026-04-20
updated: 2026-04-20
sources: [raw/articles/百万日活全端消息推送方案（Java云原生架构）.docx]
---

Locale-aware content selection inside the [[Message Template]], driven by Spring's `LocaleResolver` at send time.

## Locale tags

- ISO 639-1 identifiers: `zh_CN`, `en_US`, `ja_JP`, etc.
- All content UTF-8, no exceptions.

## Where locale data lives

- Per-template locale blocks live inside the template JSON under `content.{locale}`.
- Long-form copy is also held in a `template_i18n` table in MySQL keyed by (templateId, locale).
- Nacos Config can hot-update locale strings without a service restart.

## Resolution order

When dispatching, the system picks a locale via three-tier fallback:

1. **User setting** — whatever the user explicitly selected in app settings.
2. **Terminal default** — the device's current locale (e.g., mini-program's `wx.getSystemInfoSync().language`).
3. **System default** — `zh_CN`.

## Caveats

- The fallback only picks a locale — it does not translate. If a template has no `en_US` block, an English-locale user will see whatever the developer put as fallback (often `zh_CN`), not a machine-translated version.
- Locale strings in Nacos Config are hot-swappable but should be versioned with the template to avoid a gray window where a new placeholder is used against an old locale string.

## Related

- [[Message Template]] — where locale blocks are defined
- [[Personalization]] — runs after locale selection
- [[Push Notification Architecture]]

## Source

- [Million-DAU Push Architecture](../sources/million-dau-push-architecture.md), §3.4
