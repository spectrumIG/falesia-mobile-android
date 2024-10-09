package it.softsamurai.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.softsamurai.presentation.FalesiaItemUiState.Loading
import it.softsamurai.repository.FalesiaItemRepository
import it.softsamurai.repository.model.Falesia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FalesiaItemViewModel @Inject constructor(
    private val falesiaItemRepository: FalesiaItemRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<FalesiaItemUiState>(Loading)
    val uiState = _uiState.asStateFlow()


    init {
        retrieveFalesie()
    }

    fun retrieveFalesie() {
        viewModelScope.launch {

            falesiaItemRepository.getAllFalesie().collect {
                _uiState.value = it
            }
        }
    }


}

sealed interface FalesiaItemUiState {
    data object Loading : FalesiaItemUiState
    data class Error(val throwable: Throwable = NoSuchFieldError(), val message: String) :
        FalesiaItemUiState
    data class Success(val data: List<Falesia>) : FalesiaItemUiState
}
