package it.softsamurai.repository

import it.softsamurai.presentation.FalesiaItemUiState
import it.softsamurai.repository.network.FalesiaNetworkDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface DataSource

interface FalesiaItemRepository {
    suspend fun getAllFalesie(): Flow<FalesiaItemUiState>

}

class DefaultFalesiaRepo @Inject constructor(private val localDataSource: FalesiaNetworkDataSource) :
    FalesiaItemRepository {

    override suspend fun getAllFalesie(): Flow<FalesiaItemUiState> =
        localDataSource.getElencoFalesie()
            .map {
                if (it.isEmpty()) {
                    FalesiaItemUiState.Error(message = "ERRORE,Acciderbolina!!!")
                } else {
                    FalesiaItemUiState.Success(it)
                }
            }.catch {
                FalesiaItemUiState.Error(message = "ERRORE,Acciderbolina!!!")
            }
}
