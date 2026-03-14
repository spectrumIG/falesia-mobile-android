package it.softsamurai.presentation

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.softsamurai.domain.model.Falesia
import it.softsamurai.domain.usecase.GetAllFalesieUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FalesiaItemViewModel @Inject constructor(
    getAllFalesie: GetAllFalesieUseCase,
) : ViewModel() {

    val uiState: StateFlow<FalesiaItemUiState> = getAllFalesie()
        .map { falesie ->
            if (falesie.isEmpty()) FalesiaItemUiState.Loading
            else FalesiaItemUiState.Success(falesie)
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
    data class Success(val data: List<Falesia>) : FalesiaItemUiState
}
