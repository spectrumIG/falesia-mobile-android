# Tech stack & library policy

Source of truth for versions: [`gradle/libs.versions.toml`](../gradle/libs.versions.toml). Update the table below
when a library is added, removed or upgraded significantly.

## Current stack

| Area            | Library                                                                                                                                                | KMP-ready                                               |
|-----------------|--------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------|
| Language        | Kotlin 2.3.x, KSP                                                                                                                                      | ✅                                                      |
| Build           | AGP 9.x, Gradle version catalog, configuration cache                                                                                                   | —                                                       |
| UI              | Jetpack Compose (BOM), Material 3, `core-ui` theme module                                                                                              | ✅ via Compose Multiplatform                            |
| Navigation      | Navigation 3 (`navigation3-ui/runtime`) + Material3 adaptive-navigation3, `lifecycle-viewmodel-navigation3` (one ViewModel store per back stack entry) | ⚠️ (check CMP support at migration time)                |
| DI              | Hilt (+ `hilt-navigation-compose`, `hilt-work`)                                                                                                        | ❌ (candidates: Koin, Metro)                            |
| Persistence     | Room (KSP compiler, exported schemas)                                                                                                                  | ✅                                                      |
| Background      | WorkManager                                                                                                                                            | ❌ Android-only (keep behind `SyncScheduler` interface) |
| Scraping/HTTP   | ksoup + ksoup-network (Ktor)                                                                                                                           | ✅                                                      |
| Functional      | Arrow core + optics                                                                                                                                    | ✅                                                      |
| Serialization   | kotlinx-serialization-json                                                                                                                             | ✅                                                      |
| Images          | Coil 3 (`coil-compose`, `coil-network-okhttp`)                                                                                                         | ✅ (use `coil-network-ktor` on KMP)                     |
| Map             | MapLibre Compose 0.14 (`org.maplibre.compose:maplibre-compose`, MapLibre Native Android SDK 13 underneath) with OpenFreeMap tiles                      | ✅ (Compose Multiplatform: Android/iOS beta)            |
| Coroutines      | kotlinx-coroutines                                                                                                                                     | ✅                                                      |
| Static analysis | detekt 1.23.8 (both modules, config `detekt.yml`), Compose stability analyzer                                                                          | —                                                       |
| Tests           | JUnit4, MockK, Turbine, coroutines-test, Compose UI test, Hilt testing                                                                                 | Turbine/coroutines-test ✅, MockK ⚠️                    |

## Map

- **MapLibre Compose** is the map library: open source (BSD), OpenStreetMap vector tiles,
  Compose-first and Kotlin Multiplatform. Alternatives considered: osmdroid (Android-only, no longer
  developed), MapCompose (raster tiles only), Google Maps Compose (not OSM, API key, Android-only).
- Tiles and styles from **OpenFreeMap** (`tiles.openfreemap.org/styles/liberty` light, `/dark`
  dark): OSM data, free, no API key, no usage limits. Never point the app at
  `tile.openstreetmap.org` (its usage policy forbids apps). Attribution "© OpenStreetMap
  contributors · OpenFreeMap" is shown under the map (`map_attribution`).
- Pinned to **0.14.0**: 0.15+ need Kotlin 2.4, Compose 1.12 and `compileSdk 37`, which AGP 9.1 does
  not support (known issue). The 0.15+ API is different (overlay composables, `MapState`): the
  migration rewrites `CragMap.kt`.
- The MapLibre native library adds ~11–16 MB per ABI to the APK (all four ABIs in a universal APK);
  the consumer R8 rules come with the SDK.

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
- **Gradle daemon JVM: 21**, pinned in `gradle/gradle-daemon-jvm.properties` (regenerate with
  `./gradlew updateDaemonJvm --jvm-version=21`). If no JDK 21 is installed, Gradle downloads one into
  `~/.gradle/jdks` through the foojay resolver (`settings.gradle.kts`; settings plugins cannot use the version
  catalog, so its version is written there). Reason: detekt 1.23.x bundles a Kotlin 2.0 compiler that cannot run on
  Java 25. Revisit when moving to detekt 2.x.
- detekt tasks use `jvmTarget = 17` explicitly (by default they would use the daemon's JVM version).
- Build types: `debug` (`.debug` suffix), `release` (minify + shrink, signed with `keystore.properties`),
  `debugRelease`.
- Commands: `./gradlew assembleDebug`, `./gradlew :app:compileDebugKotlin`, `./gradlew :app:testDebugUnitTest`,
  `./gradlew detekt`.
