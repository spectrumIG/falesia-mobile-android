package it.softsamurai.domain.usecase

import it.softsamurai.domain.model.Falesia
import it.softsamurai.domain.repository.FalesiaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFalesiaWithVieUseCase @Inject constructor(
    private val repository: FalesiaRepository,
) {
    operator fun invoke(falesiaId: String): Flow<Falesia?> =
        repository.observeFalesiaWithVie(falesiaId)
}
