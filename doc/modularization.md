# Modularization

## Current modules
| Module | Type | Content |
|---|---|---|
| `:app` | Android application | Everything: domain, data (`repository`), sync, presentation |
| `:core-ui` | Android library | Compose theme (`Color.kt`, `Theme.kt`, `Type.kt`) |

## Policy
- **Do not modularize proactively.** The codebase is still small; layer separation is enforced by packages
  (see [architecture.md](architecture.md)).
- **Propose** (never execute without the user's approval) a modularization step when signals appear, e.g.:
  - a second fully implemented feature (Logbook, Ticklist, …) with its own data;
  - noticeably slow incremental builds;
  - accidental cross-layer imports (presentation → data, data entities leaking);
  - start of the KMP migration.
- Likely target shape when the time comes: `:core:domain` (pure Kotlin, KMP-ready), `:core:data`,
  `:core:database`, `:core:network`, `:core:ui`, `:feature:<name>`, and later a KMP `:shared` module.
- Any new module must reuse the version catalog and, if repetition grows, convention plugins (`build-logic`).

Update the table above whenever a module is added or removed.
