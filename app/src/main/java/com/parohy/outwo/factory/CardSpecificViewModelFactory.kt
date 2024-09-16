package com.parohy.outwo.factory

import androidx.lifecycle.*
import com.parohy.outwo.repository.CardsRepository
import com.parohy.outwo.ui.activate.ActivateViewModel
import com.parohy.outwo.ui.scratch.ScratchViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Suppress("UNCHECKED_CAST")
class CardSpecificViewModelFactory(private val cardCode: String) : AbstractSavedStateViewModelFactory(), KoinComponent {
  override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
    val cardsRepository: CardsRepository by inject()
    return when {
      modelClass.isAssignableFrom(ScratchViewModel::class.java) -> ScratchViewModel(handle, cardCode, cardsRepository) as T
      modelClass.isAssignableFrom(ActivateViewModel::class.java) -> ActivateViewModel(handle, cardCode, cardsRepository) as T
      else -> throw IllegalArgumentException("Unknown ViewModel class")
    }
  }
}