package it.softsamurai.domain.usecase

import arrow.core.Either
import arrow.core.right
import it.softsamurai.domain.error.SyncError
import it.softsamurai.domain.repository.FalesiaRepository
import it.softsamurai.domain.time.TimeProvider
import javax.inject.Inject

/** A manual refresh never re-syncs within this window from the last successful sync. */
internal const val MANUAL_REFRESH_INTERVAL_MILLIS = 24L * 60 * 60 * 1000

sealed interface RefreshOutcome {
    /** The data was synced with falesiaonline.it. */
    data object Synced : RefreshOutcome

    /** The last sync is recent: nothing was downloaded, the data already in the DB stays on screen. */
    data object AlreadyUpToDate : RefreshOutcome
}

/**
 * User-triggered refresh (pull-to-refresh). A full sync is ~110 requests to a free public site, so it runs
 * only when the DB is empty or the last successful sync is older than [MANUAL_REFRESH_INTERVAL_MILLIS].
 */
class RefreshCragsUseCase @Inject constructor(
    private val repository: FalesiaRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(): Either<SyncError, RefreshOutcome> =
        if (isRecentlySynced()) {
            RefreshOutcome.AlreadyUpToDate.right()
        } else {
            repository.syncFalesie().map { RefreshOutcome.Synced }
        }

    private suspend fun isRecentlySynced(): Boolean {
        if (repository.countFalesie() == 0) return false
        val lastSync = repository.getNewestUpdateTimestamp() ?: return false
        val age = timeProvider.nowMillis() - lastSync
        // A negative age means the device clock moved backwards: don't block refreshing forever.
        return age in 0 until MANUAL_REFRESH_INTERVAL_MILLIS
    }
}
