package com.parohy.outwo.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.parohy.outwo.core.*
import com.parohy.outwo.repository.*
import com.parohy.outwo.ui.cards.CardsViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Rule
import org.junit.jupiter.api.*
import org.junit.rules.TestRule
import org.mockito.*
import kotlin.test.*

class CardsViewModelTest {
  @Mock
  private lateinit var db: CardPreferences
  private lateinit var cardsRepository: CardsRepository
  
  @get:Rule
  val rule: TestRule = InstantTaskExecutorRule()

  @BeforeEach
  fun setup() {
    MockitoAnnotations.initMocks(this)
    cardsRepository = CardsRepositoryImpl(db)
  }

  @Test
  fun `initial state`() = runTest {
    val viewModel = CardsViewModel(SavedStateHandle(), cardsRepository, backgroundScope)

    assertTrue(viewModel.uiState.value.cards == null)
    assertTrue(viewModel.uiState.value.generate == null)
  }

  @Test
  fun `when loadCards, state should update`() = runTest {
    val viewModel = CardsViewModel(SavedStateHandle(), cardsRepository, backgroundScope)

    launch {
      viewModel.uiState.test {
        assertEquals(null, awaitItem().cards)
        assertEquals(Loading, awaitItem().cards)
        assertEquals(Content(emptyList()), awaitItem().cards)
      }
    }

    viewModel.loadCards()
    advanceUntilIdle()
  }

  @Test
  fun `when cards loading, Error should be delivered`() = runTest {
    val viewModel = CardsViewModel(SavedStateHandle(), cardsRepository, backgroundScope)

    Mockito.`when`(db.loadCards()).thenThrow(RuntimeException("Failed to load cards"))

    launch {
      viewModel.uiState.test {
        assertEquals(null, awaitItem().cards)
        assertEquals(Loading, awaitItem().cards)
        assertEquals(Failure(RuntimeException("Failed to load cards")).toString(), awaitItem().cards.toString())
      }
    }

    viewModel.loadCards()
    advanceUntilIdle()
  }

  @Test
  fun `when generate card, card generation should be Content`() = runTest {
    val viewModel = CardsViewModel(SavedStateHandle(), cardsRepository, backgroundScope)

    launch {
      viewModel.uiState.map { it.generate }.distinctUntilChanged().test {
        assertEquals(null, awaitItem())
        assertEquals(Loading, awaitItem())
        assertEquals(Content(Unit), awaitItem())
      }
    }

    viewModel.generateCard()
    advanceUntilIdle()
  }

  @Test
  fun `when generate card, card generation be exactly one card`() = runTest {
    val viewModel = CardsViewModel(SavedStateHandle(), cardsRepository, backgroundScope)

    launch {
      viewModel.uiState.map { it.cards }.distinctUntilChanged().test {
        assertEquals(null, awaitItem())
        assertEquals(1, awaitItem().valueOrNull?.size)
      }
    }

    viewModel.generateCard()
    advanceUntilIdle()
  }

  @Test
  fun `when generate card is successful, cards should update`() = runTest {
    val viewModel = CardsViewModel(SavedStateHandle(), cardsRepository, backgroundScope)

    launch {
      viewModel.uiState.map { it.cards }.distinctUntilChanged().test {
        assertEquals(null, awaitItem())
        assertNotEquals(null, awaitItem().valueOrNull)
      }
    }

    viewModel.generateCard()
    advanceUntilIdle()
  }

  @Test
  fun `given generate state Content, when clearGenerateState, generate state should be null`() = runTest {
    val viewModel = CardsViewModel(SavedStateHandle(), cardsRepository, backgroundScope)

    viewModel.generateCard()
    advanceUntilIdle()

    viewModel.clearGenerateState()

    assertTrue(viewModel.uiState.value.generate == null, "Expected null but have ${viewModel.uiState.value.generate}")
  }
}