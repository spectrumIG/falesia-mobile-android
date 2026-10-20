package it.softsamurai.domain.usecase

import it.softsamurai.domain.sync.SyncMonitor
import it.softsamurai.domain.sync.SyncStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Status of the startup sync: Discover shows an error instead of loading forever when it fails. */
class ObserveSyncStatusUseCase @Inject constructor(
    private val syncMonitor: SyncMonitor,
) {
    operator fun invoke(): Flow<SyncStatus> = syncMonitor.observeInitialSync()
}
