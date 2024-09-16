package com.parohy.outwo.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.parohy.outwo.core.*
import com.parohy.outwo.repository.*
import com.parohy.outwo.ui.activate.ActivateUiState
import com.parohy.outwo.ui.activate.ActivateViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.jupiter.api.*
import org.junit.rules.TestRule
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.*

private const val SUCCESS_CODE = 280000
private const val FAILURE_CODE = 270000

class ActivateViewModelTest {
  @Mock
  private lateinit var db: CardPreferences
  private lateinit var cardsRepository: CardsRepository

  private lateinit var viewModel: ActivateViewModel

  @get:Rule
  val rule: TestRule = InstantTaskExecutorRule()
  private val dispatcher = StandardTestDispatcher()

  private lateinit var mockWebServer: MockWebServer

  @BeforeEach
  fun setup() {
    MockitoAnnotations.initMocks(this)

    mockWebServer = MockWebServer()
    mockWebServer.start()

    cardsRepository = CardsRepositoryImpl(db, mockWebServer.url("/").toString())
  }

  @AfterEach
  fun teardown() {
    mockWebServer.shutdown()
  }

  @Test
  fun `given correct cardCode, when viewmodel is initialized, it should contain card which is not activated and is scratched`() = runTest {
    cardsRepository.generateCard()

    val cardCode = cardsRepository.getData().first().cards?.valueOrNull?.keys?.firstOrNull()
    assertTrue(cardCode != null, "Card code should not be null") //jupiter assertTrue nerobi contract, amazed

    viewModel = ActivateViewModel(SavedStateHandle(), cardCode, cardsRepository, backgroundScope)

    launch {
      viewModel.uiState.map { it.card }.test {
        assertEquals(null, awaitItem())
        assertFalse(awaitItem().valueOrNull?.isScratched ?: true, "Expected card isScratched=false")
        val item = awaitItem().valueOrNull
        assertTrue(item?.isScratched ?: false, "Expected card isScratched=true")
        assertFalse(item?.isActivated ?: true, "Expected card isActivated=false")
      }
    }

    cardsRepository.scratchCard(cardCode)
    advanceUntilIdle()
  }

  @Test
  fun `given correct cardCode, when activate called, card should update and have Content`() = runTest {
    val response = MockResponse().setResponseCode(200).setBody("{ \"android\": \"$SUCCESS_CODE\" }")
    mockWebServer.enqueue(response)

    cardsRepository = CardsRepositoryImpl(db, mockWebServer.url("/").toString(), backgroundScope)
    cardsRepository.generateCard()

    val cardCode = cardsRepository.getData().first().cards?.valueOrNull?.keys?.firstOrNull()
    assertTrue(cardCode != null, "Card code should not be null") //jupiter assertTrue nerobi contract, amazed

    viewModel = ActivateViewModel(SavedStateHandle(), cardCode, cardsRepository, backgroundScope)

    launch {
      viewModel.uiState.map { it.card }.distinctUntilChanged().test {
        assertEquals(null, awaitItem())
        val item = awaitItem()
        assertTrue(item.isContent, "Expected Content but have $item")
        assertTrue(item.valueOrNull?.isActivated ?: false, "Expected card isActivated=true")
      }
    }

    viewModel.activateCard()
    advanceUntilIdle()
  }

  @Test
  fun `given correct cardCode, when activate called, card should update and have Failure`() = runTest {
    val response = MockResponse().setResponseCode(200).setBody("{ \"android\": \"$FAILURE_CODE\" }")
    mockWebServer.enqueue(response)

    cardsRepository = CardsRepositoryImpl(db, mockWebServer.url("/").toString(), backgroundScope)
    cardsRepository.generateCard()

    val cardCode = cardsRepository.getData().first().cards?.valueOrNull?.keys?.firstOrNull()
    assertTrue(cardCode != null, "Card code should not be null") //jupiter assertTrue nerobi contract, amazed

    viewModel = ActivateViewModel(SavedStateHandle(), cardCode, cardsRepository, backgroundScope)

    launch {
      viewModel.uiState.map { it.activate }.distinctUntilChanged().test {
        assertEquals(null, awaitItem())
        val item = awaitItem()
        assertTrue(item.isFailure, "Expected Failure but have $item")
        assertEquals(Failure(IllegalStateException("Failed to activate card!")).toString(), item.toString())
      }
    }

    viewModel.activateCard()
    advanceUntilIdle()
  }
}