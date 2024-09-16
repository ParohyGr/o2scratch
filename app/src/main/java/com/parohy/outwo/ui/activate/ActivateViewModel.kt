package com.parohy.outwo.ui.activate

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.parohy.outwo.core.Loading
import com.parohy.outwo.core.State
import com.parohy.outwo.factory.CardSpecificViewModel
import com.parohy.outwo.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private const val MAX_RESULT_INT = 277028

data class ActivateUiState(
  val card: State<Throwable, ScratchCard>? = null,
  val activate: State<Throwable, Int>? = null
)

class ActivateViewModel(
  savedStateHandle: SavedStateHandle,
  private val cardCode: String
): CardSpecificViewModel(savedStateHandle, cardCode) {
  private val _uiState: MutableStateFlow<ActivateUiState> = MutableStateFlow(ActivateUiState())
  val uiState: StateFlow<ActivateUiState> = _uiState

  init {
    viewModelScope.launch {
      cardState.collectLatest { card ->
        _uiState.value = _uiState.value.copy(card = card)
      }
    }

    viewModelScope.launch {
      cardsRepository.data.collectLatest {
        _uiState.value = _uiState.value.copy(activate = it.activation)
      }
    }
  }

  fun activateCard() = cardsRepository.activateCard(cardCode)

  fun clearActivationState() = cardsRepository.resetActivate()
}