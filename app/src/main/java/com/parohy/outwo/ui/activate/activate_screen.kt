package com.parohy.outwo.ui.activate

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.parohy.outwo.core.*
import com.parohy.outwo.factory.CardSpecificViewModelFactory
import com.parohy.outwo.ui.*
import com.parohy.outwo.ui.theme.cards.CardItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivationScreenComposable(cardCode: String) {
  Scaffold(
    topBar  = {
      TopAppBar(
        title = { Text("Activate card") },
      )
    },
    content = { contentPadding ->
      val viewModel: ActivateViewModel = viewModel(factory = CardSpecificViewModelFactory(cardCode))
      val uiState = viewModel.uiState.collectAsState()

      Box(modifier = Modifier.fillMaxSize().padding(contentPadding), contentAlignment = Alignment.Center) {
        when (val card = uiState.value.card) {
          is Content -> CardItem(card = card.value, onClick = { viewModel.activateCard() })
          is Failure -> ScreenError(card.value)
          is Loading -> ScreenLoading()
          null -> {/*do nothing*/}
        }

        when (val activation = uiState.value.activate) {
          is Content -> {/*do nothing*/}
          is Failure -> Alert("Failed to activate", activation.value.message ?: "Unknown error", action = { viewModel.clearActivationState() })
          is Loading -> ScreenLoading(0.7f)
          null -> {/*do nothing*/}
        }
      }
    }
  )
}