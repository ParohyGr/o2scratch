package com.parohy.outwo.factory

import androidx.lifecycle.*
import com.parohy.outwo.repository.CardsRepository
import com.parohy.outwo.ui.cards.CardsViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Suppress("UNCHECKED_CAST")
class CardsViewModelFactory: AbstractSavedStateViewModelFactory(), KoinComponent {
  override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
    val cardsRepository: CardsRepository by inject()
    return CardsViewModel(handle, cardsRepository) as T
  }
}