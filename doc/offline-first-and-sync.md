# Offline-first & sync

## Rules
- **Room is the single source of truth.** UI and ViewModels observe **only** DB `Flow`s exposed through use cases
  (`FalesiaRepository.observeAllFalesie()`, `observeFalesiaWithVie(id)`).
- The network never feeds the UI directly: sync writes to Room, Room emits, UI updates.
- The app must be fully usable with no connectivity once data has been synced at least once.
- Sync failures are non-fatal: they surface as an `Either.Left(SyncError)` and, at most, a one-shot UI effect.

## Download mode: debug never downloads on its own
`domain/sync/DownloadMode` (provided in `DataModule` from `BuildConfig.DEBUG`):
- **Automatic** (release, `debugRelease`): everything below — startup and weekly sync, routes on crag open,
  pull-to-refresh.
- **Manual** (debug): debug builds are installed and restarted all day; automatic downloads would hammer a free
  public site. So:
  - `sync/StartupSync` cancels any scheduled sync (`SyncScheduler.cancelScheduledSyncs`) and, with an empty DB,
    `repository/fixture/FixtureDataLoader` loads the sample data: saved pages of **Monsummano** (crag list,
    crag page, map page, its 14 sector pages) in `app/src/debug/assets/fixtures/`, read by
    `FixtureNetworkDataSource` and run through the real parser and `FalesiaRepositoryImpl`. Their timestamps are
    0 ("never synced"), so the first manual download is never skipped.
  - `RefreshCragDetailsUseCase(force = false)` (crag details / route list on open) does nothing.
  - No pull-to-refresh: every screen that downloads data has a **download button** on the right of its top bar
    (`TopBarAction`, `ic_cloud_download`): Discover (crag list, `RefreshCragsUseCase`), crag details and route
    list (routes + parking, `RefreshCragDetailsUseCase(force = true)`). The 24-hour guards still apply.
  - Any new screen that downloads data must follow this rule: automatic download only in `Automatic`, a
    download button in `Manual` (ViewModels expose `manualDownloads` from `GetDownloadModeUseCase`).
- Images (Coil) and map tiles (OpenFreeMap) are loaded in both modes: they are cached and are not scraping.

## Sync pipeline
1. `SyncCragsUseCase` → `FalesiaRepositoryImpl.syncFalesie(): Either<SyncError, Unit>`:
   - fetch the crag list (one POST);
   - fetch detail pages concurrently, bounded by `SYNC_CONCURRENCY` (= 5) using `chunked` + `async/awaitAll`;
   - first failure short-circuits via `bind()`;
   - persist each crag with its **sectors** atomically (`upsertFalesiaFromSync`); DB errors →
     `SyncError.StorageFailure`. It writes a partial entity (`FalesiaSyncEntity`,
     `@Upsert(entity = …)`), so the on-demand details (parking, `vieUpdatedAt`) and the routes of
     the sectors still on the page are kept; sectors gone from the page are deleted with their
     routes (cascade).
2. `CheckDbFreshnessUseCase` returns `NeedsSync` if the DB is empty or the oldest `lastUpdated` is older than one week.
3. `SyncWorker` (`@HiltWorker`) runs freshness check → sync; retries up to `MAX_RETRIES` (3), then fails. The
   decision is the pure function `syncWorkOutcome()` (unit-tested), the worker only maps it to `Result`.
4. `WorkManagerSyncScheduler` (bound to `SyncScheduler`), invoked in `FalesiaMobile.onCreate()`:
   - `scheduleOnDemandSync()`: unique one-time work, `ExistingWorkPolicy.KEEP`, requires network;
   - `schedulePeriodicSync()`: unique periodic work every 7 days, `KEEP`, requires network.
5. Pull-to-refresh calls `RefreshCragsUseCase`: it syncs only if the DB is empty or the **last successful sync**
   (`MAX(lastUpdated)`, i.e. `FalesiaRepository.getNewestUpdateTimestamp()`) is at least 24 hours old
   (`MANUAL_REFRESH_INTERVAL_MILLIS`). Otherwise it returns `RefreshOutcome.AlreadyUpToDate` without touching the
   network: the list keeps showing the DB and the UI shows a "Data is already up to date" snackbar
   (`DiscoverEvent.AlreadyUpToDate`). A timestamp in the future (clock moved back) never blocks a refresh.
   The ViewModel ignores `onRefresh` while a refresh is running.

## Startup sync status (Discover)

- `domain/sync/SyncMonitor` reports the startup sync (`SyncStatus`: Idle, Pending, Running, Failed)
  without exposing WorkManager; `sync/WorkManagerSyncMonitor` maps the unique on-demand work with
  the pure
  `syncStatusOf(state, runAttemptCount)` (waiting after an attempt = Failed, a retry is scheduled).
  It is bound in
  `sync/di/SyncMonitorModule` (separate, so instrumented tests replace it with
  `FakeSyncMonitorModule`).
- Discover (`ObserveSyncStatusUseCase` in `FalesiaItemViewModel`, pure `reduceDiscover`): saved
  crags are always shown; with an empty DB it loads until the startup sync or a Retry fails, then
  shows
  `FalesiaItemUiState.SyncFailed` (message + Retry = `onRefresh`).
- Offline at first launch the work waits for the network (`Pending`): Discover keeps loading until
  it runs.

## On-demand crag details (routes and parking)

Routes are on the sector pages (one request per sector, ~10 per crag), so the weekly sync does not
download them.
`RefreshCragDetailsUseCase(falesiaId, force)` → `FalesiaRepositoryImpl.refreshCragDetails(id)`:

- called by crag details and the route list when they open (`force = false`): downloads when
  `vieUpdatedAt` is null or at least a week old (`ONE_WEEK_MILLIS`); pull-to-refresh on crag details
  (`force = true`) downloads when it is at least 24 hours old (`MANUAL_REFRESH_INTERVAL_MILLIS`),
  otherwise `RefreshOutcome.AlreadyUpToDate`;
- fetches the sector pages with `SYNC_CONCURRENCY`, first failure short-circuits (nothing is
  written), then the map page for the parking (optional: a failure keeps the saved parking);
- writes routes, parking and `vieUpdatedAt` in one transaction (`replaceDettagli`). Room emits, the
  screens update.
- Once opened, a crag works offline. A failed download shows an error with Retry only when there are
  no saved routes; otherwise it is silent (pull-to-refresh shows the usual snackbar).
- The map tiles are not in Room: MapLibre's own cache keeps the areas already seen (see
  [tech-stack.md](tech-stack.md)).

## Time
- Time-based rules read the clock through `domain/time/TimeProvider` (bound to `System.currentTimeMillis()` in
  `DataModule`), never `System.currentTimeMillis()` directly: tests pass a fixed time.

## Room

- `AppDatabase` (currently version 3, `exportSchema = true`), entities `FalesiaEntity`,
  `SettoreEntity`,
  `ViaEntity` (PKs are the site's sector and route ids; both cascade from the crag, routes also from
  their sector), relation `FalesiaWithVie` (crag + sectors + routes; the mapper sorts them by
  `position`, the site's order).
- Version 3 is a destructive migration (`fallbackToDestructiveMigration`, agreed with the project
  owner: the DB is a re-downloadable cache of the site). It also changed the crag ids (`monsummano`,
  no more `.html`).
- Schemas are exported to `app/schemas/` (KSP arg `room.schemaLocation`): **commit them** on every version bump.
- Schema changes: bump the version and provide a migration (prefer `AutoMigration`); never ship destructive
  migrations without asking the user.
- Room is KMP-compatible: keep DAOs `suspend`/`Flow` based and avoid Android-only APIs in them.
