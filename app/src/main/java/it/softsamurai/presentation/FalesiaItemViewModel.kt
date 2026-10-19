package it.softsamurai.presentation

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.softsamurai.domain.model.Falesia
import it.softsamurai.domain.usecase.GetAllFalesieUseCase
import it.softsamurai.domain.usecase.RefreshCragsUseCase
import it.softsamurai.domain.usecase.RefreshOutcome
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
    private val refreshCrags: RefreshCragsUseCase,
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)

    private val _events = Channel<DiscoverEvent>(Channel.BUFFERED)

    /** One-shot UI effects of a refresh (snackbars). */
    val events: Flow<DiscoverEvent> = _events.receiveAsFlow()

    /**
     * Pull-to-refresh. Ignored while a refresh is running; the list keeps showing the DB, which a successful
     * sync updates through [uiState]'s Room flow.
     */
    fun onRefresh() {
        if (_isRefreshing.value) return
        _isRefreshing.value = true
        viewModelScope.launch {
            refreshCrags().fold(
                ifLeft = { _events.send(DiscoverEvent.SyncFailed) },
                ifRight = { outcome ->
                    if (outcome == RefreshOutcome.AlreadyUpToDate) _events.send(DiscoverEvent.AlreadyUpToDate)
                },
            )
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

sealed interface DiscoverEvent {
    /** The sync failed: the saved data stays on screen. */
    data object SyncFailed : DiscoverEvent

    /** The refresh was skipped because the last sync is recent. */
    data object AlreadyUpToDate : DiscoverEvent
}

@Stable
sealed interface FalesiaItemUiState {
    data object Loading : FalesiaItemUiState
    data class Error(val throwable: Throwable = Throwable(), val message: String) :
        FalesiaItemUiState
    data class Success(val data: List<Falesia>, val isRefreshing: Boolean = false) :
        FalesiaItemUiState
}
