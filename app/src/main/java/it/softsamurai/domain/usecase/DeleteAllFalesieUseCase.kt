package it.softsamurai.domain.usecase

import arrow.core.Either
import it.softsamurai.domain.error.FalesiaError
import it.softsamurai.domain.repository.FalesiaRepository
import javax.inject.Inject

class DeleteAllFalesieUseCase @Inject constructor(
    private val repository: FalesiaRepository,
) {
    suspend operator fun invoke(): Either<FalesiaError, Unit> = repository.deleteAllFalesie()
}
