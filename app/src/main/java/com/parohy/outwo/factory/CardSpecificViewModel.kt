package com.parohy.outwo.factory

import android.util.Log
import androidx.lifecycle.*
import com.parohy.outwo.core.*
import com.parohy.outwo.repository.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@OptIn(ExperimentalCoroutinesApi::class)
open class CardSpecificViewModel(
  private val savedState: SavedStateHandle,
  private val cardCode: String
): ViewModel(), KoinComponent {
  protected val cardsRepository: CardsRepository by inject()
  private val card: MutableStateFlow<State<Throwable, ScratchCard>?> = MutableStateFlow(savedState.getCard())
  val cardState: StateFlow<State<Throwable, ScratchCard>?> = card

  init {
    Log.i("CardSpecificViewModel", "init with $cardCode")
    viewModelScope.launch {
      cardsRepository.data.mapLatest { state ->
        state.cards?.map { it.values.toList() }?.map { cards ->
          cards.find { it.code == cardCode } ?: throw IllegalArgumentException("Card $cardCode not found")
        }
      }.collectLatest { cardState ->
        Log.i("CardSpecificViewModel", "cardState $cardState")
        card.value = cardState

        cardState.ifContent { value ->
          savedState["card"] = "${value.code};${value.isScratched}"
        }
      }
    }
  }

  private fun SavedStateHandle.getCard(): State<Throwable, ScratchCard>? {
    val savedCard = get<String>("card")?.let {
      val (code, isScratched) = it.split(";")
      ScratchCard(code, isScratched.toBoolean())
    }
    Log.i("CardSpecificViewModel", "Saved card: $savedCard")
    return savedCard?.let(::Content)
  }
}