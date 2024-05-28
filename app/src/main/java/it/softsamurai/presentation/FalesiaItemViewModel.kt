/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.softsamurai.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.softsamurai.presentation.FalesiaItemUiState.Error
import it.softsamurai.presentation.FalesiaItemUiState.Loading
import it.softsamurai.presentation.FalesiaItemUiState.Success
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FalesiaItemViewModel @Inject constructor(
    private val falesiaItemRepository: it.softsamurai.core.data.FalesiaItemRepository
) : ViewModel() {

    val uiState: StateFlow<FalesiaItemUiState> = falesiaItemRepository
        .falesiaItems.map { Success(data = it) }
        .catch { Error(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Loading
        )

    fun addFalesiaItem(name: String) {
        viewModelScope.launch {
            falesiaItemRepository.add(name)
        }
    }
}

sealed interface FalesiaItemUiState {
    data object Loading : FalesiaItemUiState
    data class Error(val throwable: Throwable) : FalesiaItemUiState
    data class Success(val data: List<String>) : FalesiaItemUiState
}
