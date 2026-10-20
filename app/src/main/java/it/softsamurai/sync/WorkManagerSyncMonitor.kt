package it.softsamurai.sync

import androidx.work.WorkInfo
import androidx.work.WorkManager
import it.softsamurai.domain.sync.SyncMonitor
import it.softsamurai.domain.sync.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Startup sync status from the unique on-demand work enqueued by [WorkManagerSyncScheduler]. */
class WorkManagerSyncMonitor @Inject constructor(
    private val workManager: WorkManager,
) : SyncMonitor {

    override fun observeInitialSync(): Flow<SyncStatus> =
        workManager.getWorkInfosForUniqueWorkFlow(IMMEDIATE_SYNC_WORK)
            .map { infos ->
                infos.firstOrNull()?.let { syncStatusOf(it.state, it.runAttemptCount) }
                    ?: SyncStatus.Idle
            }
            .distinctUntilChanged()
}

/** A work waiting after an attempt is a failed attempt with a retry scheduled. */
internal fun syncStatusOf(state: WorkInfo.State, runAttemptCount: Int): SyncStatus = when (state) {
    WorkInfo.State.RUNNING -> SyncStatus.Running
    WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED ->
        if (runAttemptCount > 0) SyncStatus.Failed else SyncStatus.Pending

    WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> SyncStatus.Failed
    WorkInfo.State.SUCCEEDED -> SyncStatus.Idle
}
