package com.parohy.outwo.ui.scratch

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.parohy.outwo.core.*
import com.parohy.outwo.factory.CardSpecificViewModel
import com.parohy.outwo.repository.CardsRepository
import com.parohy.outwo.repository.ScratchCard
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ScratchUiState(
  val card: State<Throwable, ScratchCard>? = null,
  val scratch: State<Throwable, Unit>? = null
)

class ScratchViewModel(
  savedState: SavedStateHandle,
  private val cardCode: String,
  cardsRepository: CardsRepository,
  viewModelScope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
): CardSpecificViewModel(savedState, cardCode, cardsRepository, viewModelScope) {
  private val _uiState: MutableStateFlow<ScratchUiState> = MutableStateFlow(ScratchUiState())
  val uiState: StateFlow<ScratchUiState> = _uiState

  init {
    viewModelScope.launch {
      cardState.collect { card ->
        _uiState.value = _uiState.value.copy(card = card)
      }
    }
  }

  fun scratchCard() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(scratch = Loading)
      _uiState.value = try {
        cardsRepository.scratchCard(cardCode)
        _uiState.value.copy(scratch = SUCCESS)
      } catch (e: Exception) {
        _uiState.value.copy(scratch = Failure(e))
      }
    }
  }

  fun clearScratchState() {
    _uiState.value = _uiState.value.copy(scratch = null)
  }

  override fun onCleared() {
    clearScratchState()
  }
}
