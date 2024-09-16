package com.parohy.outwo.ui.scratch

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.parohy.outwo.core.*
import com.parohy.outwo.factory.CardSpecificViewModelFactory
import com.parohy.outwo.ui.*
import com.parohy.outwo.ui.cards.CardItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScratchScreenComposable(cardCode: String, goToActivation: (String) -> Unit) {
  Scaffold(
    topBar  = {
      TopAppBar(
        title = { Text("Scratch card") },
      )
    },
    content = { contentPadding ->
      val viewModel: ScratchViewModel = viewModel(factory = CardSpecificViewModelFactory(cardCode))
      val uiState = viewModel.uiState.collectAsState()

      Box(modifier = Modifier.fillMaxSize().padding(contentPadding), contentAlignment = Alignment.Center) {
        when (val card = uiState.value.card) {
          is Content -> CardItem(card = card.value, onClick = {
            if (card.value.isScratched) {
              goToActivation(card.value.code)
            } else
              viewModel.scratchCard()
          })
          is Failure -> ScreenError(card.value)
          is Loading -> ScreenLoading()
          null -> {/*do nothing*/}
        }

        val failedToScratchAlert = alertDialog(onDismiss = viewModel::clearScratchState)

        when (val scratch = uiState.value.scratch) {
          is Content -> {/*do nothing*/}
          is Failure -> failedToScratchAlert("Failed to scratch", scratch.value.message ?: "Unknown error")
          is Loading -> ScreenLoading(0.7f)
          null -> {/*do nothing*/}
        }
      }
    }
  )
}
