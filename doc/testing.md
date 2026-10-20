# Testing

## Rules
- **Every new feature or behavior change ships with tests.** A task is not done without them.
- **Prefer unit tests** (JVM, `app/src/test/`). Use instrumented/Compose UI tests (`app/src/androidTest/`) only
  for UI behavior that cannot be covered otherwise.
- Test the behavior that matters per layer:
  - **Domain**: use cases (e.g. `CheckDbFreshnessUseCaseTest`, `RefreshCragsUseCaseTest`,
    `RefreshCragDetailsUseCaseTest`) and pure model functions (`FrenchGradeTest`,
    `FalesiaStatsTest`). Inject a fixed `TimeProvider` (`{ now }`) for time-based rules and test the
    exact boundaries.
  - **Data**: repository (`FalesiaRepositoryImplTest`), mappers (`FalesiaMapperTest`), scraping/parsing helpers
    (`FalesiaHtmlParserTest` on saved pages in `app/src/test/resources/`, `ResolveImageUrlTest`; see
    [scraping.md](scraping.md)).
    - **Sync**: the worker decision `syncWorkOutcome()` (`SyncWorkOutcomeTest`), the WorkManager →
      `SyncStatus`
      mapping (`SyncStatusMappingTest`).
    - **Presentation**: ViewModels (`FalesiaItemViewModelTest`, `CragDetailsViewModelTest`,
      `RouteListViewModelTest`: the pure `reduce…`/`filterVie` functions and the intents → effects),
      navigation config
    (`NavigationConfigTest`), back stack rules (`TopLevelNavigationTest`).
    - **Resources**: EN/IT string and plural parity (`StringResourcesParityTest`, reads
      `src/main/res` from the
    module dir).
  - **Theme** (`:core-ui`): design tokens in code match `tokens.json` (`ThemeTokensTest`).
    - **UI**: `DiscoverScreenTest`, `CragDetailsScreenTest`, `RouteListScreenTest`,
      `NavigationTest`,
      `PlaceholderScreenTest` (instrumented). Screens take sample data from the previews
      (`previewCrag`).
    - **Parser on a device**: `FalesiaHtmlParserDeviceTest` (instrumented): the regexes on Android's
      ICU engine, which JVM unit tests cannot check.
    - Instrumented tests use `HiltTestApplication`, which has no WorkManager:
      `FakeSyncMonitorModule`
      (`@TestInstallIn`) replaces the sync monitor. Read
    expected texts from resources (`context.getString`), never hardcode them: tests must pass in
    every device locale. Cover light and dark (`AppTheme(darkTheme = …)`) where it matters.
- Cover both `Right` and `Left` paths of every `Either`.
- No trivial tests: skip pure delegation (a use case that only forwards to the repository); test decisions,
  boundaries, mapping and failure paths.

## Tooling & style
- JUnit4, **MockK** (`mockk`, `coEvery`), **Turbine** (`flow.test { }`) and `kotlinx-coroutines-test`
  (`runTest`, `Dispatchers.setMain(UnconfinedTestDispatcher())` / `resetMain()`).
- Prefer fakes over mocks for repositories/data sources when they grow complex.
- Inject dispatchers instead of using `Dispatchers.IO` directly so tests stay deterministic.
- Test names in backticks describing behavior: `` `getFalesiaById returns Left NotFound when crag is missing` ``.
- Arrange / Act / Assert structure; one behavior per test; no network in unit tests.

## Commands
```bash
./gradlew :core-ui:testDebugUnitTest :app:testDebugUnitTest   # unit tests
./gradlew :app:connectedDebugAndroidTest                       # instrumented tests (device/emulator required)
```

Instrumented tests run on `it.softsamurai.core.testing.HiltTestRunner` (`app/src/androidTest`).

## Visual check (manual, before merging UI changes)
Tests do not prove the app looks like the design. On an emulator, install the debug build and compare each changed
screen with `design/claude-design/` (screen markup, `tokens.json`, component READMEs) in all four combinations:
```bash
./gradlew :app:installDebug
adb shell cmd uimode night yes|no                                              # dark / light
adb shell cmd locale set-app-locales it.softsamurai.debug --locales it-IT|en-US # app language
adb exec-out screencap -p > screen.png                                          # screenshot
adb shell cmd locale set-app-locales it.softsamurai.debug --locales ""          # back to system language
```
Fix layout defects with a UI test that pins them (e.g. bounds checks in `PlaceholderScreenTest`).
