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
      mapping (`SyncStatusMappingTest`), the startup per download mode (`StartupSyncTest`), the debug sample
      data (`FixtureNetworkDataSourceTest`: fixture data source and `FixtureDataLoader` on saved pages).
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
    - **Screenshots** (JVM, Roborazzi on Robolectric, `app/src/test/.../presentation/screenshot/`):
      `DiscoverScreenshotTest`, `CragDetailsScreenshotTest`, `RouteListScreenshotTest`. They pin the look of
      each screen state (light and dark, debug download button, empty and error states) against the reference
      images in `app/src/test/snapshots/`. See "Screenshot tests" below.
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
- Compose tests use the **v2** rules (`androidx.compose.ui.test.junit4.v2.createComposeRule` /
  `createAndroidComposeRule`): effects run on a `StandardTestDispatcher`, so work is queued until the test
  waits for idle (`waitForIdle`, assertions, `mainClock`). The v1 rules are deprecated since Compose 1.12.
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

## Screenshot tests (Roborazzi)
```bash
./gradlew :app:verifyRoborazziDebug   # compare with the reference images (fails on any difference)
./gradlew :app:recordRoborazziDebug   # write the reference images after an intended UI change
./gradlew :app:compareRoborazziDebug  # write *_compare.png diffs to app/build/outputs/roborazzi/
```
- Reference images live in `app/src/test/snapshots/<TestClass>.<method>.png` and are committed: a UI change
  ships with its re-recorded images, and the reviewer looks at them in the diff.
- Write them with `composeRule.captureScreen(darkTheme) { Screen(...) }` (`ScreenshotRules.kt`): app theme,
  stateless screen fed with preview data (`previewCrag`, `previewCrags`) and pure `reduce…` functions, no
  ViewModel, no Hilt. Pick a device with `@Config(qualifiers = RobolectricDeviceQualifiers.Pixel7)`, or a tall
  one (`w411dp-h1500dp-xxhdpi`) to fit a whole scrolling page.
- They run in inspection mode, like `@Preview`: Coil loads no image (placeholders) and `CragMap` skips MapLibre,
  whose native library cannot load on the JVM (the map shows its background and overlays only). Composables that
  cannot render on the JVM must do the same with `LocalInspectionMode`.
- Robolectric runs SDK 36 with a plain `Application` (`app/src/test/resources/robolectric.properties`), not the
  Hilt app that starts WorkManager. On SDK 36 it needs `--add-opens java.base/jdk.internal.access` on the test
  JVM (`testOptions.unitTests.all` in `app/build.gradle.kts`). Fonts: Lexend is a downloadable font, so images use the fallback font.
- Screenshots complement, not replace, the instrumented tests: they check the look, the Compose tests check
  behavior and the manual visual check below the match with the design.

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
