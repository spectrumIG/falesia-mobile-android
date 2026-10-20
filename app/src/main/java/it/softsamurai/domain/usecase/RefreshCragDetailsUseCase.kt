package it.softsamurai.domain.usecase

import arrow.core.Either
import arrow.core.raise.either
import it.softsamurai.domain.error.SyncError
import it.softsamurai.domain.repository.FalesiaRepository
import it.softsamurai.domain.time.TimeProvider
import javax.inject.Inject

/**
 * Downloads a crag's routes (one request per sector) and parking when its details are shown. They are kept in
 * the DB, so the crag works offline afterwards.
 *
 * - Opening the crag ([force] = false) downloads them when missing or older than [ONE_WEEK_MILLIS].
 * - Pull-to-refresh ([force] = true) downloads them unless the last download is younger than
 *   [MANUAL_REFRESH_INTERVAL_MILLIS], like [RefreshCragsUseCase].
 */
class RefreshCragDetailsUseCase @Inject constructor(
    private val repository: FalesiaRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(
        falesiaId: String,
        force: Boolean = false
    ): Either<SyncError, RefreshOutcome> =
        either {
            val falesia = repository.getFalesiaById(falesiaId)
                .mapLeft { SyncError.CragNotFound(falesiaId) }
                .bind()
            val maxAge = if (force) MANUAL_REFRESH_INTERVAL_MILLIS else ONE_WEEK_MILLIS
            if (isFresh(falesia.vieUpdatedAt, maxAge)) {
                RefreshOutcome.AlreadyUpToDate
            } else {
                repository.refreshCragDetails(falesiaId).bind()
                RefreshOutcome.Synced
            }
        }

    private fun isFresh(updatedAt: Long?, maxAge: Long): Boolean {
        if (updatedAt == null) return false
        val age = timeProvider.nowMillis() - updatedAt
        // A negative age means the device clock moved backwards: don't block refreshing forever.
        return age in 0 until maxAge
    }
}
