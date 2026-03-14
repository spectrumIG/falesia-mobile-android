package it.softsamurai.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import it.softsamurai.domain.usecase.CheckDbFreshnessUseCase
import it.softsamurai.domain.usecase.DbFreshnessStatus
import it.softsamurai.domain.usecase.SyncCragsUseCase

private const val MAX_RETRIES = 3

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val checkDbFreshness: CheckDbFreshnessUseCase,
    private val syncCrags: SyncCragsUseCase,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return when (checkDbFreshness()) {
            DbFreshnessStatus.Fresh -> Result.success()
            DbFreshnessStatus.NeedsSync -> syncCrags()
                .fold(
                    ifLeft = {
                        if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
                    },
                    ifRight = { Result.success() },
                )
        }
    }
}
