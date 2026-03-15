package it.softsamurai.domain.usecase

import arrow.core.Either
import it.softsamurai.domain.error.SyncError
import it.softsamurai.domain.repository.FalesiaRepository
import javax.inject.Inject

class SyncCragsUseCase @Inject constructor(
    private val repository: FalesiaRepository,
) {
    suspend operator fun invoke(): Either<SyncError, Unit> = repository.syncFalesie()
}
