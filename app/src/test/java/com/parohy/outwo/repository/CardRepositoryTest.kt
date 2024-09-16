package com.parohy.outwo.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.parohy.outwo.core.*
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
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val SUCCESS_CODE = 280000
private const val FAILURE_CODE = 270000

class CardRepositoryTest {
  @Mock
  private lateinit var db: CardPreferences
  private lateinit var cardsRepository: CardsRepository

  @get:Rule
  val rule: TestRule = InstantTaskExecutorRule()
  private val dispatcher = StandardTestDispatcher()

  private lateinit var mockWebServer: MockWebServer

  @BeforeEach
  fun setup() {
    MockitoAnnotations.initMocks(this)
    cardsRepository = CardsRepositoryImpl(db)

    Dispatchers.setMain(dispatcher)
  }

  @AfterEach
  fun teardown() {
    Dispatchers.resetMain()
    if (::mockWebServer.isInitialized)
      mockWebServer.shutdown()
  }

  @Test
  fun `initial state`() = runTest {
    assertEquals(cardsRepository.getData().first(), CardRepositoryState())
  }

  @Test
  fun `when loadCards, state should update`() = runTest {
    val results = mutableListOf<CardRepositoryState>()
    val job = launch {
      cardsRepository.getData().toList(results)
    }

    cardsRepository.loadCards()
    advanceUntilIdle()

    job.cancel()

    assertTrue(results.last().cards.isContent)
  }

  @Test
  fun `when generateCard, state should update`() = runTest {
    val results = mutableListOf<CardRepositoryState>()
    val job = launch {
      cardsRepository.getData().toList(results)
    }

    cardsRepository.generateCard()
    advanceUntilIdle()

    job.cancel()

    assertTrue(results.last().cards.isContent)
  }

  @Test
  fun `when scratchCard, state should update`() = runTest {
    val results = mutableListOf<CardRepositoryState>()
    val job = launch {
      cardsRepository.getData().toList(results)
    }

    cardsRepository.generateCard()
    cardsRepository.scratchCard(cardsRepository.getData().first().cards.valueOrNull?.keys?.first() ?: "123")
    advanceUntilIdle()

    job.cancel()

    assertTrue(results.last().cards.valueOrNull?.values?.lastOrNull()?.isScratched ?: false, "Expected scratched card but ${results.last().cards.valueOrNull?.values?.lastOrNull()?.isScratched}")
  }

  @Test
  fun `when scratchCard with not existing card, function should throw`() = runTest {
    val ex: IllegalArgumentException = assertThrows { cardsRepository.scratchCard("123") }
    assertEquals(ex.message, "Card 123 not found")
  }

  @Test
  fun `when removeCard, state should update`() = runTest {
    val results = mutableListOf<CardRepositoryState>()
    val job = launch {
      cardsRepository.getData().toList(results)
    }

    cardsRepository.generateCard()
    cardsRepository.removeCard(cardsRepository.getData().first().cards.valueOrNull?.keys?.first() ?: "123")
    advanceUntilIdle()

    job.cancel()

    assertTrue(results.last().cards.valueOrNull?.values?.isEmpty() ?: false, "Expected empty cards but ${results.last().cards.valueOrNull?.values}")
  }

  @Test
  fun `when removeCard with not existing card, function should throw`() = runTest {
    val ex: IllegalArgumentException = assertThrows { cardsRepository.removeCard("123") }
    assertEquals(ex.message, "Card 123 not found")
  }

  @Test
  fun `given correct result, when activateCard, state should update Content`() = runTest {
    mockWebServer = MockWebServer()
    mockWebServer.start()

    val response = MockResponse().setResponseCode(200).setBody("{ \"android\": \"$SUCCESS_CODE\" }")
    mockWebServer.enqueue(response)

    cardsRepository = CardsRepositoryImpl(db, mockWebServer.url("/").toString(), TestScope(dispatcher))

    cardsRepository.generateCard()
    val cardCode = cardsRepository.getData().first().cards.valueOrNull?.keys?.firstOrNull()
    assertTrue(cardCode != null, "Card code should not be null")

    val results = mutableListOf<CardRepositoryState>()
    val job = launch {
      cardsRepository.getData().toList(results)
    }

    cardsRepository.activateCard(cardCode)

    advanceUntilIdle()

    job.cancel()

    println(results)
    assertTrue(results.last().activation.isContent, "Expected Content but have ${results.last().activation}")
  }

  @Test
  fun `given failure result, when activateCard, state should update Failure`() = runTest {
    mockWebServer = MockWebServer()
    mockWebServer.start()

    val response = MockResponse().setResponseCode(200).setBody("{ \"android\": \"$FAILURE_CODE\" }")
    mockWebServer.enqueue(response)

    cardsRepository = CardsRepositoryImpl(db, mockWebServer.url("/").toString(), TestScope(dispatcher))

    cardsRepository.generateCard()
    val cardCode = cardsRepository.getData().first().cards.valueOrNull?.keys?.firstOrNull()
    assertTrue(cardCode != null, "Card code should not be null")

    val results = mutableListOf<CardRepositoryState>()
    val job = launch {
      cardsRepository.getData().toList(results)
    }

    cardsRepository.activateCard(cardCode)

    advanceUntilIdle()

    job.cancel()

    println(results)
    assertTrue(results.last().activation.isFailure, "Expected Failure but have ${results.last().activation}")
    assertEquals("Failed to activate card!", results.last().activation.failureOrNull?.message)
  }
}