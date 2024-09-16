package com.parohy.outwo.ui.scratch

import android.util.Log
import androidx.lifecycle.*
import com.parohy.outwo.core.*
import com.parohy.outwo.repository.*
import com.parohy.outwo.factory.CardSpecificViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class ScratchUiState(
  val card: State<Throwable, ScratchCard>? = null,
  val scratch: State<Throwable, Unit>? = null
)

class ScratchViewModel(
  savedState: SavedStateHandle,
  private val cardCode: String
): CardSpecificViewModel(savedState, cardCode) {
  private val _uiState: MutableStateFlow<ScratchUiState> = MutableStateFlow(ScratchUiState())
  val uiState: StateFlow<ScratchUiState> = _uiState

  init {
    viewModelScope.launch {
      cardState.collectLatest { card ->
        _uiState.value = _uiState.value.copy(card = card)
      }
    }
  }

  fun scratchCard() {
    Log.i("ScratchViewModel", "scratchCard $cardCode")
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(scratch = Loading)
      cardsRepository.scratchCard(cardCode)
      _uiState.value = _uiState.value.copy(scratch = SUCCESS)
    }
  }

  fun clearScratchState() {
    _uiState.value = _uiState.value.copy(scratch = null)
  }
}
