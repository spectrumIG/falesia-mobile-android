package it.softsamurai.domain.usecase

import it.softsamurai.domain.repository.FalesiaRepository
import it.softsamurai.domain.time.TimeProvider
import javax.inject.Inject

internal const val ONE_WEEK_MILLIS = 7L * 24 * 60 * 60 * 1000

sealed interface DbFreshnessStatus {
    data object NeedsSync : DbFreshnessStatus
    data object Fresh : DbFreshnessStatus
}

class CheckDbFreshnessUseCase @Inject constructor(
    private val repository: FalesiaRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(): DbFreshnessStatus {
        if (repository.countFalesie() == 0) return DbFreshnessStatus.NeedsSync
        val oldest = repository.getOldestUpdateTimestamp() ?: return DbFreshnessStatus.NeedsSync
        val age = timeProvider.nowMillis() - oldest
        return if (age > ONE_WEEK_MILLIS) DbFreshnessStatus.NeedsSync else DbFreshnessStatus.Fresh
    }
}
