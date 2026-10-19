# Offline-first & sync

## Rules
- **Room is the single source of truth.** UI and ViewModels observe **only** DB `Flow`s exposed through use cases
  (`FalesiaRepository.observeAllFalesie()`, `observeFalesiaWithVie(id)`).
- The network never feeds the UI directly: sync writes to Room, Room emits, UI updates.
- The app must be fully usable with no connectivity once data has been synced at least once.
- Sync failures are non-fatal: they surface as an `Either.Left(SyncError)` and, at most, a one-shot UI effect.

## Sync pipeline
1. `SyncCragsUseCase` → `FalesiaRepositoryImpl.syncFalesie(): Either<SyncError, Unit>`:
   - fetch the crag list (one POST);
   - fetch detail pages concurrently, bounded by `SYNC_CONCURRENCY` (= 5) using `chunked` + `async/awaitAll`;
   - first failure short-circuits via `bind()`;
   - persist each crag with its routes atomically (`upsertFalesiaWithVie`); DB errors → `SyncError.StorageFailure`.
2. `CheckDbFreshnessUseCase` returns `NeedsSync` if the DB is empty or the oldest `lastUpdated` is older than one week.
3. `SyncWorker` (`@HiltWorker`) runs freshness check → sync; retries up to `MAX_RETRIES` (3), then fails.
4. `WorkManagerSyncScheduler` (bound to `SyncScheduler`), invoked in `FalesiaMobile.onCreate()`:
   - `scheduleOnDemandSync()`: unique one-time work, `ExistingWorkPolicy.KEEP`, requires network;
   - `schedulePeriodicSync()`: unique periodic work every 7 days, `KEEP`, requires network.
5. Pull-to-refresh in the UI calls `SyncCragsUseCase` directly.

## Room
- `AppDatabase` (currently version 2, `exportSchema = true`), entities `FalesiaEntity`, `ViaEntity`,
  relation `FalesiaWithVie`.
- Schemas are exported to `app/schemas/` (KSP arg `room.schemaLocation`): **commit them** on every version bump.
- Schema changes: bump the version and provide a migration (prefer `AutoMigration`); never ship destructive
  migrations without asking the user.
- Room is KMP-compatible: keep DAOs `suspend`/`Flow` based and avoid Android-only APIs in them.
