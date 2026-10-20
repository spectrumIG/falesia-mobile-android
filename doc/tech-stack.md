# Tech stack & library policy

Source of truth for versions: [`gradle/libs.versions.toml`](../gradle/libs.versions.toml). Update the table below
when a library is added, removed or upgraded significantly.

## Current stack

| Area            | Library                                                                                                                                                | KMP-ready                                               |
|-----------------|--------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------|
| Language        | Kotlin 2.4.x, KSP 2.3 (KSP2)                                                                                                                           | ✅                                                      |
| Build           | AGP 9.4, Gradle 9.8, version catalog, configuration cache                                                                                                   | —                                                       |
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
| Tests           | JUnit4, MockK, Turbine, coroutines-test, Compose UI test, Hilt testing, Roborazzi + Robolectric (screenshots) | Turbine/coroutines-test ✅, MockK ⚠️, Roborazzi ✅ (Compose Multiplatform desktop/iOS) |

## Map

- **MapLibre Compose** is the map library: open source (BSD), OpenStreetMap vector tiles,
  Compose-first and Kotlin Multiplatform. Alternatives considered: osmdroid (Android-only, no longer
  developed), MapCompose (raster tiles only), Google Maps Compose (not OSM, API key, Android-only).
- Tiles and styles from **OpenFreeMap** (`tiles.openfreemap.org/styles/liberty` light, `/dark`
  dark): OSM data, free, no API key, no usage limits. Never point the app at
  `tile.openstreetmap.org` (its usage policy forbids apps). Attribution "© OpenStreetMap
  contributors · OpenFreeMap" is shown under the map (`map_attribution`).
- Pinned to **0.14.0** (works with Kotlin 2.4, Compose 1.12, `compileSdk 37`). The build blockers of
  0.15+ are gone, but 0.20 is a rewrite of the library (new MlnFfi renderer, `MapState` instead of
  `CameraState`, `MapUiOptions`/`MapInteractions` instead of `MapOptions`, new style and camera API):
  upgrading means rewriting `CragMap.kt` and checking the map on a device (known issue).
- The MapLibre native library adds ~11–16 MB per ABI to the APK (all four ABIs in a universal APK);
  the consumer R8 rules come with the SDK.

## Library selection policy
1. Use the **latest stable** Android/Kotlin technologies (alphas/betas only when needed and justified; none
   today). Kotlin stays on the latest stable release (2.5 is still in beta).
2. **Prefer KMP-compatible libraries** whenever an equivalent exists (KMP conversion is a planned milestone).
   E.g. ksoup over Jsoup, Ktor over Retrofit/OkHttp, kotlinx-serialization over Gson/Moshi, kotlinx-datetime over
   `java.time`, Room KMP, Coil 3.
3. Android-only libraries must be isolated behind interfaces (as done with `SyncScheduler`).
4. All dependencies go through the version catalog (`libs.*`); never hardcode versions in `build.gradle.kts`.
5. Ask the user before introducing a new major library or replacing an existing one.

## Build basics
- `compileSdk = 37` (Compose 1.12 and adaptive 1.3 require it), `minSdk = 30`, Java/JVM target 17.
- **Gradle daemon JVM: 21**, pinned in `gradle/gradle-daemon-jvm.properties` (regenerate with
  `./gradlew updateDaemonJvm --jvm-version=21`). If no JDK 21 is installed, Gradle downloads one into
  `~/.gradle/jdks` through the foojay resolver (`settings.gradle.kts`; settings plugins cannot use the version
  catalog, so its version is written there). Reason: detekt 1.23.x bundles a Kotlin 2.0 compiler that cannot run on
  Java 25. Revisit when moving to detekt 2.x (only alphas so far). detekt 1.23 also causes Gradle's "deprecated
  features, incompatible with Gradle 10" warning (`Configuration.setVisible`, `ReportingExtension.file`).
- KSP 2.3 works with AGP's built-in Kotlin: no `android.disallowKotlinSourceSets` workaround.
- detekt tasks use `jvmTarget = 17` explicitly (by default they would use the daemon's JVM version).
- Build types: `debug` (`.debug` suffix), `release` (minify + shrink, signed with `keystore.properties`),
  `debugRelease`.
- **R8 on release** (`isMinifyEnabled` + `isShrinkResources`): code is shrunk, optimized and obfuscated.
  Libraries bring their own consumer rules; `app/proguard-rules.pro` only holds what the app reaches by
  reflection:
  - the Navigation 3 destinations `AppRoute$*`: the back stack is saved by class name and restored with
    `Class.forName` + their serializers;
  - line numbers for stack traces (`SourceFile,LineNumberTable`, source file renamed).
  Never add blanket rules (`-keep class kotlin.** { *; }` and the like): add a rule only for a class reached
  by reflection, with a comment, and check it on a release build (see the release smoke test in
  [testing.md](testing.md)). R8 must build with no missing-class warnings (no `-dontwarn` today).
- Each release build writes `app/build/outputs/mapping/release/mapping.txt`: keep it with the release
  (the AAB carries it to Play) and decode obfuscated traces with
  `$ANDROID_HOME/cmdline-tools/latest/bin/retrace mapping.txt stacktrace.txt`.
- Commands: `./gradlew assembleDebug`, `./gradlew :app:compileDebugKotlin`, `./gradlew :app:testDebugUnitTest`,
  `./gradlew detekt`.
