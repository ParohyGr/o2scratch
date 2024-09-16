package com.parohy.outwo.ui.theme.cards

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.parohy.outwo.R
import com.parohy.outwo.core.*
import com.parohy.outwo.repository.*
import com.parohy.outwo.ui.*
import com.parohy.outwo.ui.cards.CardsViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CardsScreenComposable(goToCardDetail: (ScratchCard) -> Unit) {
  val viewModel: CardsViewModel = viewModel()
  Scaffold(
    topBar  = {
      TopAppBar(
        title = { Text("Cards") },
        actions = {
          IconButton(onClick = { viewModel.generateCard() }) {
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
                  CardItem(modifier = Modifier.animateItem(), card = card, onClick = { goToCardDetail(card) })
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

          null ->
            LaunchedEffect(Unit) { viewModel.loadCards() }
        }

        when(val generate = uiState.value.generate) {
          is Content -> {/*do nothing*/}
          is Failure ->
            Alert(title = "Failed to generate card", text = generate.value.message ?: "Unknown error") {
              viewModel.clearGenerateState()
            }
          is Loading -> ScreenLoading(0.7f)
          null -> {/*do nothing*/}
        }
      }
    }
  )
}

@Composable
fun CardItem(modifier: Modifier = Modifier, card: ScratchCard, onClick: () -> Unit) {
  Surface(
    modifier = Modifier.size(300.dp, 220.dp).then(modifier),
    shape    = RoundedCornerShape(8.dp),
    color    = MaterialTheme.colorScheme.background,
    border   = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
  ) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
      if (card.isScratched) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(text = card.code, style = MaterialTheme.typography.titleMedium)
          Button(onClick = onClick) {
            Text("Activate")
          }
        }
      }
      else
        Button(onClick = onClick) {
          Text("Scratch")
        }
    }
  }
}
