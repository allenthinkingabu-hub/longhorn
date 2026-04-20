---
title: Wiki Operation Log
type: overview
tags: [meta, log]
created: 2026-04-20
updated: 2026-04-20
sources: []
---

Append-only chronological record of every wiki operation. Parseable with `grep "^## \[" wiki/log.md`.

## [2026-04-20] init | Wiki scaffolded

Created directory layout: `raw/`, `wiki/{concepts,entities,sources,queries}/`. Wrote `schema.md`, `index.md`, `overview.md`, `log.md`, and a project-level `CLAUDE.md` pointing at the wiki. `raw/` is empty — no INGEST performed.

## [2026-04-20] schema | Added raw/ subfolders by type

Added subfolders under `raw/`: `articles/`, `papers/`, `notes/`, `pasted/`, `images/`. Updated `schema.md` directory layout section and changelog. User preference: they want physical organization of sources by type rather than a flat `raw/` directory.

## [2026-04-20] ingest | Million-DAU Multi-Platform Push Architecture (Java Cloud-Native)

Ingested `raw/articles/百万日活全端消息推送方案（Java云原生架构）.docx` — AI-generated Chinese architecture proposal by 豆包AI. Created 17 pages:

- 1 source: `sources/million-dau-push-architecture.md`
- 11 concepts: push-notification-architecture, message-template, user-segmentation, task-scheduling, offline-push, multi-channel-fallback, personalization, internationalization, observability, reliability, compliance
- 5 entities: netty-socketio, powerjob, vendor-push-channels, wechat-subscribe-message, doubao-ai

Updated `index.md` to catalog all new pages and rewrote `overview.md` with themes, a concept map, and open questions.

## [2026-04-20] query | Implementation plan (7-phase, 13-15 week rollout)

User requested a full landing plan covering design / development / testing / deployment. Produced `落地实施计划.md` (~630 lines, in Chinese, targeting technical team execution). Key additions beyond the source: quantitative gray-rollout gates, APNs single-message cache handling, phase-0 legal front-loading, 50 ms personalization budget risk treatment. Filed a pointer at `wiki/queries/implementation-plan.md` and updated index.

## [2026-04-20] move | Implementation plan relocated to raw/notes/

Moved `落地实施计划.md` from project root to `raw/notes/落地实施计划.md` at user's request, consistent with the `raw/` subfolder convention for written notes. Rewrote all `](wiki/...)` links inside the plan to `](../../wiki/...)` so cross-references still resolve from the new location. Updated `wiki/queries/implementation-plan.md` to point at the new path.
