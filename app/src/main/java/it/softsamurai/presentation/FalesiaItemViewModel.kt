package it.softsamurai.presentation

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.softsamurai.domain.model.Falesia
import it.softsamurai.domain.usecase.GetAllFalesieUseCase
import it.softsamurai.domain.usecase.SyncCragsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FalesiaItemViewModel @Inject constructor(
    getAllFalesie: GetAllFalesieUseCase,
    private val syncCrags: SyncCragsUseCase,
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)

    private val _syncErrorEvent = Channel<Unit>(Channel.BUFFERED)
    val syncErrorEvent: Flow<Unit> = _syncErrorEvent.receiveAsFlow()

    fun onRefresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val result = syncCrags()
            if (result.isLeft()) _syncErrorEvent.send(Unit)
            _isRefreshing.value = false
        }
    }

    val uiState: StateFlow<FalesiaItemUiState> = combine(
        getAllFalesie(),
        _isRefreshing,
    ) { falesie, isRefreshing ->
        if (falesie.isEmpty()) FalesiaItemUiState.Loading
        else FalesiaItemUiState.Success(falesie, isRefreshing)
    }
        .catch { emit(FalesiaItemUiState.Error(it, it.message ?: "Unknown error")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FalesiaItemUiState.Loading,
        )
}

@Stable
sealed interface FalesiaItemUiState {
    data object Loading : FalesiaItemUiState
    data class Error(val throwable: Throwable = Throwable(), val message: String) :
        FalesiaItemUiState
    data class Success(val data: List<Falesia>, val isRefreshing: Boolean = false) :
        FalesiaItemUiState
}
