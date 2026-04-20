# Helix-longfeng

## Wiki

This project has an LLM wiki at `wiki/`. Consult `wiki/index.md` for questions about the wiki's domain. Keep the wiki updated as new sources arrive.

- Drop source documents into `raw/` — they are immutable once ingested.
- Ingest new sources by reading them, then creating/updating pages in `wiki/concepts/`, `wiki/entities/`, `wiki/sources/`.
- Every operation appends to `wiki/log.md` and updates `wiki/index.md`.
- See `wiki/schema.md` for page conventions.
