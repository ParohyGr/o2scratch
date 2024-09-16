package com.parohy.outwo.factory

import androidx.lifecycle.*
import com.parohy.outwo.ui.activate.ActivateViewModel
import com.parohy.outwo.ui.scratch.ScratchViewModel

@Suppress("UNCHECKED_CAST")
class CardSpecificViewModelFactory(private val cardCode: String) : AbstractSavedStateViewModelFactory() {
  override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T =
    when {
      modelClass.isAssignableFrom(ScratchViewModel::class.java) -> ScratchViewModel(handle, cardCode) as T
      modelClass.isAssignableFrom(ActivateViewModel::class.java) -> ActivateViewModel(handle, cardCode) as T
      else -> throw IllegalArgumentException("Unknown ViewModel class")
    }
}