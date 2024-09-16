package com.parohy.outwo.ui.activate

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.parohy.outwo.core.State
import com.parohy.outwo.factory.CardSpecificViewModel
import com.parohy.outwo.repository.CardsRepository
import com.parohy.outwo.repository.ScratchCard
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class ActivateUiState(
  val card: State<Throwable, ScratchCard>? = null,
  val activate: State<Throwable, Unit>? = null
)

class ActivateViewModel(
  savedStateHandle: SavedStateHandle,
  private val cardCode: String,
  cardsRepository: CardsRepository,
  viewModelScope: CoroutineScope = CoroutineScope( Dispatchers.Main + SupervisorJob())
): CardSpecificViewModel(savedStateHandle, cardCode, cardsRepository, viewModelScope) {
  private val _uiState: MutableStateFlow<ActivateUiState> = MutableStateFlow(ActivateUiState())
  val uiState: StateFlow<ActivateUiState> = _uiState

  init {
    viewModelScope.launch {
      cardState.collect { card ->
        _uiState.value = _uiState.value.copy(card = card)
      }
    }

    viewModelScope.launch {
      cardsRepository.getData().collect {
        _uiState.value = _uiState.value.copy(activate = it.activation)
      }
    }
  }

  fun activateCard() = cardsRepository.activateCard(cardCode)

  fun clearActivationState() = cardsRepository.resetActivate()

  override fun onCleared() {
    clearActivationState()
  }
}