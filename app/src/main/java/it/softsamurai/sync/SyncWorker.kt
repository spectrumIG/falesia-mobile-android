package it.softsamurai.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import arrow.core.Either
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import it.softsamurai.domain.error.SyncError
import it.softsamurai.domain.usecase.CheckDbFreshnessUseCase
import it.softsamurai.domain.usecase.DbFreshnessStatus
import it.softsamurai.domain.usecase.SyncCragsUseCase

internal const val MAX_RETRIES = 3

/** What a background sync run should report to WorkManager. */
internal enum class SyncOutcome { Success, Retry, Failure }

/**
 * Decision of one background sync run: skip when the data is fresh, otherwise sync and retry failures
 * until [MAX_RETRIES] attempts were made. [sync] runs only when needed.
 */
internal suspend fun syncWorkOutcome(
    freshness: DbFreshnessStatus,
    runAttemptCount: Int,
    sync: suspend () -> Either<SyncError, Unit>,
): SyncOutcome = when (freshness) {
    DbFreshnessStatus.Fresh -> SyncOutcome.Success
    DbFreshnessStatus.NeedsSync -> sync().fold(
        ifLeft = { if (runAttemptCount < MAX_RETRIES) SyncOutcome.Retry else SyncOutcome.Failure },
        ifRight = { SyncOutcome.Success },
    )
}

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val checkDbFreshness: CheckDbFreshnessUseCase,
    private val syncCrags: SyncCragsUseCase,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result =
        when (syncWorkOutcome(checkDbFreshness(), runAttemptCount) { syncCrags() }) {
            SyncOutcome.Success -> Result.success()
            SyncOutcome.Retry -> Result.retry()
            SyncOutcome.Failure -> Result.failure()
        }
}
