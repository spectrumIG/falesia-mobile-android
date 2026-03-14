package it.softsamurai.domain.usecase

import arrow.core.Either
import it.softsamurai.domain.error.FalesiaError
import it.softsamurai.domain.repository.FalesiaRepository
import javax.inject.Inject

class DeleteFalesiaUseCase @Inject constructor(
    private val repository: FalesiaRepository,
) {
    suspend operator fun invoke(falesiaId: String): Either<FalesiaError, Unit> =
        repository.deleteFalesia(falesiaId)
}
