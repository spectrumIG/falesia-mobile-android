# Project overview

## Purpose
Falesia Mobile is an **informational, read-only** app for climbers: a catalogue of crags (*falesie*) and their
routes (*vie*). Users do not create remote content; the app consumes public data and presents it well, even offline.

## Data source
- Data is **scraped** from [falesiaonline.it](https://www.falesiaonline.it), a public and free website.
- Crag list: POST to `https://www.falesiaonline.it/falesie/sud-europa.html` (body
  `location_id=1&order=byinterest&shownnumberofcrags=110`, `Referer` header set).
- Crag details (description, photo, sectors, coordinates): one GET per crag detail page.
- Routes and parking: one GET per sector page and one map page, downloaded when the crag is opened.
- Details in [scraping.md](scraping.md).

## Core principles
- **Offline-first**: the local Room database is the single source of truth; the network only refreshes it.
  See [offline-first-and-sync.md](offline-first-and-sync.md).
- **Clean Architecture + MVI**: see [architecture.md](architecture.md) and [mvi.md](mvi.md).
- **Functional style** with Arrow: see [functional-programming.md](functional-programming.md).
- **Latest Android tech, KMP-friendly libraries**: see [tech-stack.md](tech-stack.md).
- **Tested**: every feature ships with tests, unit tests first. See [testing.md](testing.md).

## Current features

| Section (bottom bar) | Route                             | Status                                                                                                         |
|----------------------|-----------------------------------|----------------------------------------------------------------------------------------------------------------|
| Discover             | `AppRoute.Discover`               | Implemented: crag list, pull-to-refresh, crag cards (design-system tokens, light + dark)                       |
| ↳ Crag details       | `AppRoute.CragDetails(falesiaId)` | Implemented: hero, quick info, description, OSM map with sectors and parking, Open navigation, featured routes |
| ↳ Route list         | `AppRoute.RouteList(falesiaId)`   | Implemented: routes by sector, search, grade/sector/style filters                                              |
| Logbook              | `AppRoute.Logbook`                | Placeholder (`PlaceholderScreen`)                                                                              |
| Ticklist             | `AppRoute.Ticklist`               | Placeholder (`PlaceholderScreen`)                                                                              |
| Profile              | `AppRoute.Profile`                | Placeholder (`PlaceholderScreen`)                                                                              |

UI languages: English (default, `values/`) and Italian (`values-it/`).

## Roadmap / milestones

1. Rework the UI in **Claude Design** (the Stitch mockup is imported there and its tokens are mapped
   in
   `:core-ui`).
2. Fill the placeholder sections.
3. Convert the project to **Kotlin Multiplatform** (KMP). Every library/design choice made today should ease this.

Keep this file updated when features or milestones change.
