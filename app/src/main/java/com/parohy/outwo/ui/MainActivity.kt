package com.parohy.outwo.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.*
import androidx.navigation.compose.*
import com.parohy.outwo.ui.activate.ActivationScreenComposable
import com.parohy.outwo.ui.theme.O2ScratchTheme
import com.parohy.outwo.ui.theme.cards.CardsScreenComposable
import com.parohy.outwo.ui.scratch.ScratchScreenComposable
import kotlinx.serialization.Serializable

@Serializable
data object CardsScreen
@Serializable
data class ScratchScreen(val cardCode: String)
@Serializable
data class ActivationScreen(val cardCode: String)


class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val navController = rememberNavController()

      O2ScratchTheme {
        NavHost(navController = navController, startDestination = CardsScreen) {
          composable<CardsScreen> {
            CardsScreenComposable { card ->
              navController.navigate(
                route = if (card.isScratched)
                  ActivationScreen(card.code)
                else
                  ScratchScreen(card.code)
              )
            }
          }

          composable<ScratchScreen> {
            val dest: ScratchScreen = it.toRoute()
            ScratchScreenComposable(dest.cardCode) {
              navController.popBackStack()
              navController.navigate(ActivationScreen(dest.cardCode))
            }
          }

          composable<ActivationScreen> {
            val dest: ActivationScreen = it.toRoute()
            ActivationScreenComposable(dest.cardCode)
          }
        }
      }
    }
  }
}