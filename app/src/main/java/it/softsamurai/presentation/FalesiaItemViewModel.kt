package it.softsamurai.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.softsamurai.presentation.FalesiaItemUiState.Error
import it.softsamurai.presentation.FalesiaItemUiState.Loading
import it.softsamurai.presentation.FalesiaItemUiState.Success
import it.softsamurai.repository.FalesiaItemRepository
import it.softsamurai.repository.model.Falesia
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FalesiaItemViewModel @Inject constructor(
    falesiaItemRepository: FalesiaItemRepository
) : ViewModel() {

    init {

        viewModelScope.launch {
            falesiaItemRepository.getAllFalesie()
        }
    }

    val uiState: StateFlow<FalesiaItemUiState> = falesiaItemRepository
        .falesiaItems.map { Success(data = it) }
        .catch { Error(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Loading
        )
}

sealed interface FalesiaItemUiState {
    data object Loading : FalesiaItemUiState
    data class Error(val throwable: Throwable) : FalesiaItemUiState
    data class Success(val data: List<Falesia>) : FalesiaItemUiState
}
