# Known issues & tech debt

Keep this list current: add findings as you discover them, remove items when fixed.

| # | Issue | Where | Suggested fix |
|---|---|---|---|
| 1 | ViewModel not yet MVI (no `onIntent`/`Effect`) | `app/src/main/java/it/softsamurai/presentation/FalesiaItemViewModel.kt` | Migrate to the contract in [mvi.md](mvi.md), update its test |
| 2 | Instrumented test runner class does not exist | `testInstrumentationRunner = "it.softsamurai.core.testing.HiltTestRunner"` in `app/build.gradle.kts` | Create `HiltTestRunner` (extends `AndroidJUnitRunner`, uses `HiltTestApplication`) in `app/src/androidTest` |
| 3 | detekt config path mismatch | `app/build.gradle.kts` points to `gradle/detekt.yml`, file is at repo root `detekt.yml` | Move the file or fix the path |
| 4 | Stale Room schema folder with typo | `app/schemas/it.softsamurai.reposiitory.database.AppDatabase/` | Delete after confirming it is unused |
| 5 | Domain depends on `javax.inject.Inject` (KMP blocker) | `domain/usecase/*` | Decide at KMP milestone (DI provided from data/app module or KMP DI library) |
| 6 | `SyncError.ParseFailure` defined but not produced by the scraper | `repository/network/FalesiaNetworkDataSource.kt` | Raise it on unexpected markup instead of silent defaults where data is essential |
| 7 | `System.currentTimeMillis()` used directly in domain/data | `CheckDbFreshnessUseCase`, `FalesiaRepositoryImpl` | Inject a clock (`kotlinx-datetime` `Clock`) for testability and KMP |
