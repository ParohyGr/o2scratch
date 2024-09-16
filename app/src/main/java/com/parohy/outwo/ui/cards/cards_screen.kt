package com.parohy.outwo.ui.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.parohy.outwo.R
import com.parohy.outwo.core.*
import com.parohy.outwo.factory.CardsViewModelFactory
import com.parohy.outwo.repository.ScratchCard
import com.parohy.outwo.ui.*
import com.parohy.outwo.ui.cards.CardsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsScreenComposable(goToCardDetail: (ScratchCard) -> Unit) {
  val viewModel: CardsViewModel = viewModel(factory = CardsViewModelFactory())

  Scaffold(
    topBar  = {
      TopAppBar(
        title = { Text("Cards") },
        actions = {
          IconButton(onClick = viewModel::generateCard) {
            Icon(painter = painterResource(R.drawable.ic_add_card), contentDescription = "Add card")
          }
        }
      )
    },
    content = { contentPadding ->
      val uiState = viewModel.uiState.collectAsState()
      Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when (val cards = uiState.value.cards) {
          is Content ->
            if (cards.value.isNotEmpty())
              LazyColumn(
                modifier            = Modifier.fillMaxWidth().padding(contentPadding),
                contentPadding      = PaddingValues(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                items(cards.value.toList(), ScratchCard::code) { card ->
                  CardItem(
                    modifier = Modifier.animateItem(),
                    card     = card,
                    onClick  = { goToCardDetail(card) },
                    delete   = { viewModel.removeCard(card.code) }
                  )
                }
              }
            else
              Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text("No cards available", style = MaterialTheme.typography.titleLarge)
                  Text("Generate new card by clicking on the + icon")
                }
              }

          is Failure -> ScreenError(cards.value)

          is Loading -> ScreenLoading()

          null -> LaunchedEffect(Unit) { viewModel.loadCards() }
        }

        val failedToGenerate = alertDialog(onDismiss = viewModel::clearGenerateState)

        when(val generate = uiState.value.generate) {
          is Failure -> failedToGenerate("Failed to generate card", generate.value.message ?: "Unknown error")
          is Loading -> ScreenLoading(0.7f)
          else       -> {/*do nothing*/}
        }
      }
    }
  )
}

@Composable
fun CardItem(modifier: Modifier = Modifier, card: ScratchCard, onClick: () -> Unit, delete: (() -> Unit)? = null) {
  val deleteAlert = alertDialog(confirm = delete)

  Surface(
    modifier = Modifier.size(300.dp, 220.dp).then(modifier),
    shape    = RoundedCornerShape(8.dp),
    color    = MaterialTheme.colorScheme.background,
    border   = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
  ) {
    Box {
      Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        if (card.isScratched) {
          Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = card.code, style = MaterialTheme.typography.titleMedium)
            Button(onClick = onClick, enabled = !card.isActivated) {
              Text("Activate")
            }
          }
        }
        else
          Button(onClick = onClick) {
            Text("Scratch")
          }
      }

      if (card.isScratched)
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
          if (card.isActivated)
            Text(
              modifier = Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 8.dp),
              text = "Card is activated",
              style = MaterialTheme.typography.bodySmall.copy(color = Color.Green)
            )
          else
            Spacer(modifier = Modifier.weight(1f))

          if (delete != null)
            IconButton(onClick = { deleteAlert("Delete card", "Are you sure you want to delete this card?") }) {
              Icon(painter = painterResource(R.drawable.ic_delete), contentDescription = "Delete card")
            }
        }
    }
  }
}
