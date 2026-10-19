# Tech stack & library policy

Source of truth for versions: [`gradle/libs.versions.toml`](../gradle/libs.versions.toml). Update the table below
when a library is added, removed or upgraded significantly.

## Current stack
| Area | Library | KMP-ready |
|---|---|---|
| Language | Kotlin 2.3.x, KSP | ✅ |
| Build | AGP 9.x, Gradle version catalog, configuration cache | — |
| UI | Jetpack Compose (BOM), Material 3, `core-ui` theme module | ✅ via Compose Multiplatform |
| Navigation | Navigation 3 (`navigation3-ui/runtime`) + Material3 adaptive-navigation3 | ⚠️ (check CMP support at migration time) |
| DI | Hilt (+ `hilt-navigation-compose`, `hilt-work`) | ❌ (candidates: Koin, Metro) |
| Persistence | Room (KSP compiler, exported schemas) | ✅ |
| Background | WorkManager | ❌ Android-only (keep behind `SyncScheduler` interface) |
| Scraping/HTTP | ksoup + ksoup-network (Ktor) | ✅ |
| Functional | Arrow core + optics | ✅ |
| Serialization | kotlinx-serialization-json | ✅ |
| Images | Coil 3 (`coil-compose`, `coil-network-okhttp`) | ✅ (use `coil-network-ktor` on KMP) |
| Coroutines | kotlinx-coroutines | ✅ |
| Static analysis | detekt, Compose stability analyzer | — |
| Tests | JUnit4, MockK, Turbine, coroutines-test, Compose UI test, Hilt testing | Turbine/coroutines-test ✅, MockK ⚠️ |

## Library selection policy
1. Use the **latest stable** Android/Kotlin technologies (alphas only when needed and justified, e.g.
   adaptive-navigation3).
2. **Prefer KMP-compatible libraries** whenever an equivalent exists (KMP conversion is a planned milestone).
   E.g. ksoup over Jsoup, Ktor over Retrofit/OkHttp, kotlinx-serialization over Gson/Moshi, kotlinx-datetime over
   `java.time`, Room KMP, Coil 3.
3. Android-only libraries must be isolated behind interfaces (as done with `SyncScheduler`).
4. All dependencies go through the version catalog (`libs.*`); never hardcode versions in `build.gradle.kts`.
5. Ask the user before introducing a new major library or replacing an existing one.

## Build basics
- `compileSdk = 36`, `minSdk = 30`, Java/JVM target 17.
- Build types: `debug` (`.debug` suffix), `release` (minify + shrink, signed with `keystore.properties`),
  `debugRelease`.
- Commands: `./gradlew assembleDebug`, `./gradlew :app:compileDebugKotlin`, `./gradlew :app:testDebugUnitTest`,
  `./gradlew detekt`.
