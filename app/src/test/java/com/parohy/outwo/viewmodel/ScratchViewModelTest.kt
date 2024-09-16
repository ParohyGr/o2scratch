package com.parohy.outwo.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.parohy.outwo.core.*
import com.parohy.outwo.repository.*
import com.parohy.outwo.ui.scratch.ScratchUiState
import com.parohy.outwo.ui.scratch.ScratchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.Rule
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.rules.TestRule
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.*

class ScratchViewModelTest {
  @Mock
  private lateinit var db: CardPreferences
  private lateinit var cardsRepository: CardsRepository

  private lateinit var viewModel: ScratchViewModel

  @get:Rule
  val rule: TestRule = InstantTaskExecutorRule()
  private val dispatcher = StandardTestDispatcher()

  @BeforeEach
  fun setup() {
    MockitoAnnotations.initMocks(this)

    cardsRepository = CardsRepositoryImpl(db)

    Dispatchers.setMain(dispatcher)
  }

  @AfterEach
  fun teardown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `given correct cardCode, when viewmodel is initialized, it should contain card which is not scratched`() = runTest {
    cardsRepository.generateCard()

    val cardCode = cardsRepository.getData().first().cards?.valueOrNull?.keys?.firstOrNull()
    assertTrue(cardCode != null, "Card code should not be null") //jupiter assertTrue nerobi contract, amazed

    viewModel = ScratchViewModel(SavedStateHandle(), cardCode, cardsRepository)
    advanceUntilIdle()

    viewModel.uiState.map { it.card }.test {
      val item = awaitItem()
      assertTrue(item.isContent, "Content should be present but $item")
      assertFalse(item.valueOrNull?.isScratched ?: true, "Card isScratched=false but $item")
    }
  }

  @Test
  fun `given correct cardCode, when scratchCard called, card should update`() = runTest {
    cardsRepository.generateCard()

    val cardCode = cardsRepository.getData().first().cards?.valueOrNull?.keys?.firstOrNull()
    assertTrue(cardCode != null, "Card code should not be null")

    viewModel = ScratchViewModel(SavedStateHandle(), cardCode, cardsRepository)

    launch {
      viewModel.uiState.map { it.card }.distinctUntilChanged().test {
      assertEquals(null, awaitItem())
      val item = awaitItem()
      assertTrue(item.isContent, "Content should be present but $item")
      assertFalse(item.valueOrNull?.isScratched ?: true, "Card isScratched=false but $item")
      val item2 = awaitItem()
      assertTrue(item2.isContent, "Content should be present but $item2")
      assertTrue(item2.valueOrNull?.isScratched ?: false, "Card isScratched=true but $item2")
      }
    }

    viewModel.scratchCard()
    advanceUntilIdle()
  }

  @Test
  fun `given correct cardCode, when scratchCard called, scratch state should update`() = runTest {
    cardsRepository.generateCard()

    val cardCode = cardsRepository.getData().first().cards?.valueOrNull?.keys?.firstOrNull()
    assertTrue(cardCode != null, "Card code should not be null")

    viewModel = ScratchViewModel(SavedStateHandle(), cardCode, cardsRepository)

    launch {
      viewModel.uiState.map { it.scratch }.distinctUntilChanged().test {
        assertEquals(null, awaitItem())
        assertEquals(Loading, awaitItem())
        val item = awaitItem()
        assertTrue(item.isContent, "Expected Content but have $item")
      }
    }

    viewModel.scratchCard()
    advanceUntilIdle()
  }
}