package com.parohy.outwo.factory

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.parohy.outwo.core.*
import com.parohy.outwo.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Rule
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.rules.TestRule
import org.mockito.*
import kotlin.test.*

class CardSpecificViewModelTest {
  @Mock
  private lateinit var db: CardPreferences
  private lateinit var cardsRepository: CardsRepository

  private lateinit var viewModel: CardSpecificViewModel

  @get:Rule
  val rule: TestRule = InstantTaskExecutorRule()

  @BeforeEach
  fun setup() {
    MockitoAnnotations.initMocks(this)

    cardsRepository = CardsRepositoryImpl(db)
  }

  @Test
  fun `given correct cardCode, when viewmodel is initialized, it should contain card`() = runTest {
    cardsRepository.generateCard()

    val cardCode = cardsRepository.getData().first().cards?.valueOrNull?.keys?.firstOrNull()
    assertTrue(cardCode != null, "Card code should not be null")

    viewModel = CardSpecificViewModel(SavedStateHandle(), cardCode, cardsRepository, backgroundScope)
    advanceUntilIdle()

    viewModel.cardState.test {
      assertEquals(null, awaitItem())
      val item = awaitItem()
      assertTrue(item.isContent, "Content should be present but $item")
    }
  }

  @Test
  fun `given no cards loaded, when viewmodel is initialized, it should contain Failure`() = runTest {
    val cardCode = "cardCode"
    viewModel = CardSpecificViewModel(SavedStateHandle(), cardCode, cardsRepository, backgroundScope)
    advanceUntilIdle()

    viewModel.cardState.test {
      assertEquals(null, awaitItem())
      val item = awaitItem()
      assertTrue(item is Failure, "Failure should be present but $item")
      assertEquals("Cards state not loaded", item.failureOrNull?.message)
    }
  }

  @Test
  fun `given cards loaded and empty, when viewmodel is initialized, it should contain Failure`() = runTest {
    Mockito.`when`(db.loadCards()).thenReturn(emptySet())
    cardsRepository.loadCards()

    val cardCode = "cardCode"
    viewModel = CardSpecificViewModel(SavedStateHandle(), cardCode, cardsRepository, backgroundScope)
    advanceUntilIdle()

    viewModel.cardState.test {
      assertEquals(null, awaitItem())
      val item = awaitItem()
      assertTrue(item is Failure, "Failure should be present but $item")
      assertEquals("There are no cards", item.failureOrNull?.message)
    }
  }

  @Test
  fun `given incorrect cardCode, when viewmodel is initialized, it should contain Failure`() = runTest {
    cardsRepository.generateCard()

    val cardCode = "cardCode"
    viewModel = CardSpecificViewModel(SavedStateHandle(), cardCode, cardsRepository, backgroundScope)
    advanceUntilIdle()

    viewModel.cardState.test {
      assertEquals(null, awaitItem())
      val item = awaitItem()
      assertTrue(item is Failure, "Failure should be present but $item")
      assertEquals("Card $cardCode not found", item.failureOrNull?.message)
    }
  }
}