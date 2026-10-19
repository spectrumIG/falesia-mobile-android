# AGENTS.md

Falesia Mobile is an **offline-first, informational Android app** for climbers. It shows crags (*falesie*) and
routes (*vie*) scraped from the public, free site [falesiaonline.it](https://www.falesiaonline.it).
Architecture: **Clean Architecture + MVI**, UI in **Jetpack Compose**, functional style with **Arrow**,
libraries chosen with a future **Kotlin Multiplatform** migration in mind.

This file is an **index only**. All guidance lives in topic files under [`doc/`](doc/). Read the files relevant
to your task before changing code.

## Index

| Topic | File | Read it when… |
|---|---|---|
| Project overview & roadmap | [doc/project-overview.md](doc/project-overview.md) | Always, first. Purpose, data source, features, milestones |
| Architecture (Clean Architecture) | [doc/architecture.md](doc/architecture.md) | Adding classes, deciding which layer/package code belongs to |
| MVI presentation pattern | [doc/mvi.md](doc/mvi.md) | Writing or changing a ViewModel / screen state |
| Offline-first & sync | [doc/offline-first-and-sync.md](doc/offline-first-and-sync.md) | Touching Room, repository, WorkManager sync, data freshness |
| Scraping falesiaonline.it | [doc/scraping.md](doc/scraping.md) | Touching network data sources, HTML parsing, selectors |
| Functional programming (Arrow) | [doc/functional-programming.md](doc/functional-programming.md) | Error handling, `Either`/`Raise`, optics, concurrency style |
| Tech stack & library policy | [doc/tech-stack.md](doc/tech-stack.md) | Adding/upgrading a dependency, build setup |
| UI (Compose) & design | [doc/ui-compose.md](doc/ui-compose.md) | Any UI work, theme, navigation, design system |
| Testing | [doc/testing.md](doc/testing.md) | Every change: what and how to test |
| Modularization | [doc/modularization.md](doc/modularization.md) | Thinking about new Gradle modules |
| Code quality & workflow | [doc/code-quality-and-workflow.md](doc/code-quality-and-workflow.md) | Before finishing any task: checks, git, naming, secrets |
| Known issues & tech debt | [doc/known-issues.md](doc/known-issues.md) | Before fixing "weird" things; add new findings here |

## Maintenance rule (mandatory)

These docs are part of the codebase and must evolve with it:

- Any change to architecture, libraries, modules, conventions, build or known issues **must update the relevant
  `doc/` file in the same change**.
- A new topic gets a **new file in `doc/`** and a **new row in the index above**. Keep this file an index:
  no guidance here, only links (relative paths, never absolute).
- When you fix an item listed in [doc/known-issues.md](doc/known-issues.md), remove it from there.
- If a doc contradicts the code, trust the code, fix the doc, and mention it in your report.
