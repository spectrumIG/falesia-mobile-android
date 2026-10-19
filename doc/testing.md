# Testing

## Rules
- **Every new feature or behavior change ships with tests.** A task is not done without them.
- **Prefer unit tests** (JVM, `app/src/test/`). Use instrumented/Compose UI tests (`app/src/androidTest/`) only
  for UI behavior that cannot be covered otherwise.
- Test the behavior that matters per layer:
  - **Domain**: use cases (e.g. `CheckDbFreshnessUseCaseTest`, `SyncCragsUseCaseTest`).
  - **Data**: repository (`FalesiaRepositoryImplTest`), mappers (`FalesiaMapperTest`), scraping/parsing helpers
    (`ResolveImageUrlTest`; add HTML fixture tests for selectors, see [scraping.md](scraping.md)).
  - **Presentation**: ViewModels (`FalesiaItemViewModelTest`), navigation config
    (`NavigationConfigTest`), back stack rules (`TopLevelNavigationTest`).
  - **Resources**: EN/IT string parity (`StringResourcesParityTest`, reads `src/main/res` from the
    module dir).
  - **Theme** (`:core-ui`): design tokens in code match `tokens.json` (`ThemeTokensTest`).
  - **UI**: `DiscoverScreenTest`, `NavigationTest`, `PlaceholderScreenTest` (instrumented). Read
    expected texts from resources (`context.getString`), never hardcode them: tests must pass in
    every device locale. Cover light and dark (`AppTheme(darkTheme = …)`) where it matters.
- Cover both `Right` and `Left` paths of every `Either`.

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
