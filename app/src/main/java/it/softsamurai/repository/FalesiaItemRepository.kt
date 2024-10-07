package it.softsamurai.repository

import it.softsamurai.repository.model.Falesia
import it.softsamurai.repository.network.FalesiaNetworkDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

interface DataSource

interface FalesiaItemRepository {
    val falesiaItems: Flow<List<Falesia>>
    suspend fun getAllFalesie(): Flow<List<Falesia>>

}

class DefaultFalesiaRepo @Inject constructor(private val localDataSource: FalesiaNetworkDataSource) :
    FalesiaItemRepository {
    private var _falesieItem: Flow<List<Falesia>> = MutableStateFlow(emptyList())

    override val falesiaItems: Flow<List<Falesia>>
        get() = _falesieItem

    override suspend fun getAllFalesie(): Flow<List<Falesia>> =
        flowOf(localDataSource.getElencoFalesie())
}
