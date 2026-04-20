---
title: Message Template
type: concept
tags: [template, json-schema, i18n, versioning]
created: 2026-04-20
updated: 2026-04-20
sources: [raw/articles/百万日活全端消息推送方案（Java云原生架构）.docx]
---

JSON-Schema-backed template definition that separates message *content* from *format validation*, supports [[Internationalization]] inline, and carries its own version + lifecycle.

## Template shape

Each template is a JSON document with four blocks:

- **Metadata** — `templateId`, `type` (one of SYSTEM_NOTICE, BUSINESS_NOTICE, MARKETING, RECOMMENDATION), `version`, `status` (ACTIVE / INACTIVE / DRAFT).
- **Content** — keyed by locale (`zh_CN`, `en_US`, …), each with `title`, `content`, `buttons`, `style`. Fields support `{placeholder}` variables for [[Personalization]].
- **Schema** — an embedded JSON Schema that validates whatever data you hand in before rendering. Catches missing fields, bad URLs, oversized strings at submit time instead of at send time.
- **Timestamps** — `createTime`, `updateTime`.

## Lifecycle

Five states, gated by review:

1. **Draft** — editable, never sent.
2. **Review** — automated schema check + human compliance review.
3. **Active** — eligible for real traffic. Supports rollback to a prior version.
4. **Gray** — new version is served to a configurable percentage of users; rollout ratios live in Nacos Config so they can move without a deploy. Delivery and click-through are monitored; auto-abort on regression.
5. **Retired** — evicted from cache, no longer dispatched.

## Why JSON Schema and not codegen?

- **Config over code.** New message types don't require a new Java class; ops can ship a template without a service deploy.
- **Shared validator.** The same schema runs at template-submit time and at send time — submission errors and runtime errors are the same class of error.
- **Gray-safe.** Schema and content are versioned together, so a gray rollout is coherent (no "new template, old validator" split-brain).

## Related

- [[Personalization]] — what fills the `{placeholder}` variables
- [[Internationalization]] — the locale keys
- [[Push Notification Architecture]] — where the template management service sits
- [[Compliance]] — the review step

## Source

- [Million-DAU Push Architecture](../sources/million-dau-push-architecture.md), §3.1 "模板管理模块"
