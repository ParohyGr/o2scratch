package com.parohy.outwo.ui.cards

import android.util.Log
import androidx.lifecycle.*
import com.parohy.outwo.core.*
import com.parohy.outwo.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class CardsUiState(
  val cards: State<Throwable, List<ScratchCard>>? = null,
  val generate: State<Throwable, Unit>? = null
)

class CardsViewModel(private val savedState: SavedStateHandle): ViewModel(), KoinComponent {
  private val cardsRepository: CardsRepository by inject()
  private val _uiState: MutableStateFlow<CardsUiState> = MutableStateFlow(CardsUiState(cards = savedState.getCards()))
  val uiState: StateFlow<CardsUiState> = _uiState

  init {
    viewModelScope.launch {
      cardsRepository.data.collectLatest { data ->
        val cards = data.cards?.map { it.values.toList() }

        _uiState.value = _uiState.value.copy(cards = cards)

        cards.ifContent { value ->
          savedState["cards"] = value.map { "${it.code};${it.isScratched}" }.toTypedArray()
        }
      }
    }
  }

  fun loadCards() {
    viewModelScope.launch {
      cardsRepository.loadCards()
    }
  }

  fun generateCard() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(generate = Loading)
      cardsRepository.generateCard()
      _uiState.value = _uiState.value.copy(generate = SUCCESS)
    }
  }

  fun clearGenerateState() {
    _uiState.value = _uiState.value.copy(generate = null)
  }

  private fun SavedStateHandle.getCards(): State<Throwable, List<ScratchCard>>? {
    val savedCards = get<Array<String>>("cards")?.map {
      val (code, isScratched) = it.split(";")
      ScratchCard(code, isScratched.toBoolean())
    }
    Log.i("CardsViewModel", "Saved cards: $savedCards")
    return savedCards?.let { Content(it) }
  }
}