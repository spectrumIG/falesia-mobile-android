# Architecture

The app follows **Clean Architecture** with an **MVI** presentation layer ([mvi.md](mvi.md)).
Root package: `it.softsamurai` (module `:app`, see [modularization.md](modularization.md)).

## Layers and packages

| Layer | Package | Content |
|---|---|---|
| Domain | `domain.model` | Immutable domain models (`Falesia`, `Via`), annotated with Arrow `@optics` |
| Domain | `domain.error` | Typed sealed errors (`FalesiaError`, `SyncError`) |
| Domain | `domain.repository` | Repository interfaces (`FalesiaRepository`) |
| Domain | `domain.usecase` | One use case per action, `operator fun invoke` (`GetAllFalesieUseCase`, `SyncCragsUseCase`, `CheckDbFreshnessUseCase`, …) |
| Data | `repository` | `FalesiaRepositoryImpl` (implements the domain interface) |
| Data | `repository.local` | `FalesiaLocalDataSource` over Room |
| Data | `repository.database` | Room `AppDatabase`, `dao`, `entity`, `relation`, `di` |
| Data | `repository.network` | `FalesiaNetworkDataSource` (ksoup scraping) + `network.model` |
| Data | `repository.mapper` | Pure mapping functions network ↔ entity ↔ domain |
| Data | `repository.di` | Hilt bindings (`DataModule`) |
| Background | `sync` | WorkManager `SyncWorker`, `SyncScheduler` |
| Presentation | `presentation` | ViewModels, `MainActivity`, `screen/` composables, `navigation/` |
| Shared DI | `core.di` | Qualifiers (`@IoDispatcher`) |

Note: the data layer package is named `repository` (historical). Keep using it for new data code until a
modularization step renames it (ask first).

## Dependency rule
- `presentation` → `domain` ← `repository` (data). Domain depends on **nothing** in data or presentation.
- Domain must stay **pure Kotlin**: no Android, Room, Compose or ksoup imports. Allowed: Kotlin stdlib,
  coroutines `Flow`, Arrow. `javax.inject.Inject` is currently used on use cases (tolerated, but a KMP blocker:
  see [known-issues.md](known-issues.md)).
- ViewModels talk to **use cases**, never directly to repositories or data sources.
- Room entities and network models never leave the data layer: always map to domain models.

## Data flow
```
falesiaonline.it ──ksoup──▶ network models ──mapper──▶ Room entities ──Flow──▶ mapper ──▶ domain models
                                                             ▲                              │
                                       SyncWorker / refresh ─┘              use case ◀──────┘
                                                                                │
                                                          ViewModel (UiState) ──▶ Compose UI
```

## Adding a feature (checklist)
1. Domain model / error types in `domain`.
2. Repository interface method + use case.
3. Data implementation (local and, if needed, network + mapper).
4. ViewModel following [mvi.md](mvi.md), then Compose screen ([ui-compose.md](ui-compose.md)).
5. Tests for each layer ([testing.md](testing.md)).
6. Update `doc/` ([../AGENTS.md](../AGENTS.md) maintenance rule).
