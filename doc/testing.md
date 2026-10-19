# Testing

## Rules
- **Every new feature or behavior change ships with tests.** A task is not done without them.
- **Prefer unit tests** (JVM, `app/src/test/`). Use instrumented/Compose UI tests (`app/src/androidTest/`) only
  for UI behavior that cannot be covered otherwise.
- Test the behavior that matters per layer:
  - **Domain**: use cases (e.g. `CheckDbFreshnessUseCaseTest`, `SyncCragsUseCaseTest`).
  - **Data**: repository (`FalesiaRepositoryImplTest`), mappers (`FalesiaMapperTest`), scraping/parsing helpers
    (`ResolveImageUrlTest`; add HTML fixture tests for selectors, see [scraping.md](scraping.md)).
  - **Presentation**: ViewModels (`FalesiaItemViewModelTest`), navigation config (`NavigationConfigTest`).
  - **UI**: `DiscoverScreenTest`, `NavigationTest` (instrumented).
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
./gradlew :app:testDebugUnitTest            # unit tests
./gradlew :app:connectedDebugAndroidTest    # instrumented tests (device/emulator required)
```
Known issue: the instrumented runner class is missing, see [known-issues.md](known-issues.md).
