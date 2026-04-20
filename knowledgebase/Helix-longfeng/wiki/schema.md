---
title: Wiki Schema & Conventions
type: overview
tags: [meta, schema]
created: 2026-04-20
updated: 2026-04-20
sources: []
---

Conventions for how this wiki is structured and maintained. Co-evolves with the user as the wiki grows.

## Changelog

- **2026-04-20** — Initial scaffold.
- **2026-04-20** — Added `raw/` subfolders by source type (articles, papers, notes, pasted, images).

## Directory layout

- `raw/` — immutable source documents. Never edited after ingest. Organized into subfolders by type:
  - `raw/articles/` — blog posts, web articles, news pieces
  - `raw/papers/` — academic / technical papers, typically PDFs
  - `raw/notes/` — personal notes, meeting transcripts, internal writeups
  - `raw/pasted/` — ad-hoc text pasted into the session, saved to a file
  - `raw/images/` — screenshots, diagrams, figures
  - If a source doesn't fit cleanly, place it in the closest match and let the source page's `tags` encode nuance. A file's semantic meaning lives in `wiki/sources/<name>.md`, not in its folder.
- `wiki/index.md` — catalog of every page with a one-line summary.
- `wiki/log.md` — append-only chronological record of every INIT / INGEST / QUERY / LINT operation.
- `wiki/schema.md` — this file; conventions for the wiki.
- `wiki/overview.md` — high-level synthesis across all sources. Updated whenever a new source shifts the big picture.
- `wiki/concepts/` — one page per concept (abstract idea, technique, pattern).
- `wiki/entities/` — one page per concrete entity (person, org, product, paper, dataset).
- `wiki/sources/` — one summary page per ingested source, mirroring a file in `raw/`.
- `wiki/queries/` — filed query results worth keeping.

## Page frontmatter

Every wiki page starts with YAML frontmatter:

```yaml
---
title: Page Title
type: concept | entity | source | query | overview
tags: [tag1, tag2]
created: YYYY-MM-DD
updated: YYYY-MM-DD
sources: [raw/filename.md]
---
```

## Writing conventions

- Start every page with a 1–2 sentence summary after the frontmatter.
- Cross-reference other pages with `[[wikilinks]]` using the page title.
- Cite sources with `[source](../sources/name.md)` or direct paths to raw files.
- Keep pages atomic — one concept/entity per page. Split when pages grow over ~400 lines.
- Prefer short, declarative claims. Long passages should be summarized, not transcribed.

## Index.md format

Entries are grouped by category (Overview, Concepts, Entities, Sources, Queries). Each line:

```
- [Page Title](path/to/page.md) — one-line summary (N sources, YYYY-MM-DD)
```

## Log.md format

Append-only. One section per operation:

```
## [YYYY-MM-DD] operation | Title
```

Parseable with `grep "^## \[" wiki/log.md`.

## Naming

- File names: kebab-case, matching page title (e.g., `attention-is-all-you-need.md`).
- Concept pages use singular noun (`attention.md`, not `attentions.md`).
- Source pages mirror the `raw/` filename when reasonable.
