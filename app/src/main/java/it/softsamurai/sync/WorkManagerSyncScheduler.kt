package it.softsamurai.sync

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val IMMEDIATE_SYNC_WORK = "falesia_immediate_sync"
private const val PERIODIC_SYNC_WORK = "falesia_periodic_sync"

class WorkManagerSyncScheduler @Inject constructor(
    private val workManager: WorkManager,
) : SyncScheduler {

    private val networkConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    override fun scheduleOnDemandSync() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(networkConstraints)
            .build()
        workManager.enqueueUniqueWork(
            IMMEDIATE_SYNC_WORK,
            ExistingWorkPolicy.KEEP,
            request,
        )
    }

    override fun schedulePeriodicSync() {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(7, TimeUnit.DAYS)
            .setConstraints(networkConstraints)
            .build()
        workManager.enqueueUniquePeriodicWork(
            PERIODIC_SYNC_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}
