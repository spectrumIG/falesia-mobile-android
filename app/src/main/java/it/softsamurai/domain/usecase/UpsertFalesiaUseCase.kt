package it.softsamurai.domain.usecase

import arrow.core.Either
import it.softsamurai.domain.error.FalesiaError
import it.softsamurai.domain.model.Falesia
import it.softsamurai.domain.repository.FalesiaRepository
import javax.inject.Inject

class UpsertFalesiaUseCase @Inject constructor(
    private val repository: FalesiaRepository,
) {
    suspend operator fun invoke(falesia: Falesia): Either<FalesiaError, Unit> =
        repository.upsertFalesia(falesia)
}
