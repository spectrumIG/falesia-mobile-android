package it.softsamurai.domain.sync

import kotlinx.coroutines.flow.Flow

/** State of the sync that fills the DB when the app starts. */
enum class SyncStatus {
    /** Done, or nothing to do. */
    Idle,

    /** Waiting to start, e.g. for network connectivity. */
    Pending,
    Running,

    /** The last attempt failed (a retry may be scheduled). */
    Failed,
}

/** Reports the background sync, without exposing how it is scheduled (WorkManager, in the `sync` package). */
fun interface SyncMonitor {
    fun observeInitialSync(): Flow<SyncStatus>
}
