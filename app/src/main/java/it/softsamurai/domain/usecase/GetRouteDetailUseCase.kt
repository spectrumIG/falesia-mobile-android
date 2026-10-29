package it.softsamurai.domain.usecase

import it.softsamurai.domain.model.RouteDetail
import it.softsamurai.domain.model.vieOfSettore
import it.softsamurai.domain.repository.FalesiaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Observes route `viaId` of crag `falesiaId` with its sector, the sector's routes and the sector's topo, from the
 * DB. Emits null when the crag or the route is not there (e.g. the route left the site after a refresh).
 */
class GetRouteDetailUseCase @Inject constructor(
    private val repository: FalesiaRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(falesiaId: String, viaId: Long): Flow<RouteDetail?> =
        repository.observeFalesiaWithVie(falesiaId).flatMapLatest { crag ->
            val via = crag?.vie?.find { it.id == viaId }
            if (crag == null || via == null) {
                flowOf(null)
            } else {
                repository.observeTopo(via.settoreId).map { topo ->
                    RouteDetail(
                        crag = crag,
                        via = via,
                        settore = crag.settori.find { it.id == via.settoreId },
                        sectorVie = crag.vieOfSettore(via.settoreId),
                        topo = topo,
                    )
                }
            }
        }
}
