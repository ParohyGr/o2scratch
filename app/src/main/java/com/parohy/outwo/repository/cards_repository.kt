package com.parohy.outwo.repository

import com.parohy.outwo.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import java.util.UUID

data class ScratchCard(
  val code: String,
  val isScratched: Boolean = false,
  val isActivated: Boolean = false
)

interface CardsRepository {
  suspend fun loadCards()
  suspend fun generateCard()
  suspend fun scratchCard(code: String)
  fun activateCard(code: String)
  fun resetActivate()
  fun removeCard(code: String)
  fun getData(): Flow<CardRepositoryState>
}

data class CardRepositoryState(
  val cards: State<Throwable, Map<String, ScratchCard>>? = null,
  val activation: State<Throwable, Unit>? = null
)

private const val MIN_RESULT_INT = 277028

class CardsRepositoryImpl(
  private val db: CardPreferences,
  private val apiEndpoint: String = "https://api.o2.sk/",
  private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
): CardsRepository {
  private val _data: MutableStateFlow<CardRepositoryState> = MutableStateFlow(CardRepositoryState())
  override fun getData(): Flow<CardRepositoryState> = _data

  private val cardsMap: Map<String, ScratchCard> get() = (_data.value.cards.valueOrNull ?: emptyMap())

  private val httpClient by lazy {
    OkHttpClient.Builder()
      .addInterceptor(HttpLoggingInterceptor().apply { level = Level.BODY })
      .build()
  }

  override suspend fun loadCards() {
    if (_data.value.cards.isLoading) return

    withContext(Dispatchers.IO) {
      if (!_data.value.cards.isContent) //here could handle refresh state
        _data.value = _data.value.copy(cards = Loading)

      delay(2000)
      _data.value = try {
        _data.value.copy(cards = Content(db.loadCards().associateBy(ScratchCard::code)))
      } catch (e: Exception) {
        _data.value.copy(cards = Failure(RuntimeException("Failed to load cards", e)))
      }
    }
  }

  override suspend fun generateCard() {
    withContext(Dispatchers.IO) {
      delay(2000)
      val uuid = UUID.randomUUID().toString()
      val newCards = cardsMap + (uuid to ScratchCard(uuid))
      db.saveCards(newCards.values.toList())
      _data.value = _data.value.copy(cards = Content(newCards))
    }
  }

  override suspend fun scratchCard(code: String) {
    withContext(Dispatchers.IO) {
      delay(2000)
      val cardToScratch = cardsMap[code]?.copy(isScratched = true) ?: throw IllegalArgumentException("Card $code not found")
      val newCards = cardsMap + (code to cardToScratch)
      db.saveCards(newCards.values.toList())
      _data.value = _data.value.copy(cards = Content(newCards))
    }
  }

  override fun activateCard(code: String) {
    if (_data.value.activation.isLoading) return

    coroutineScope.launch {
      _data.value = _data.value.copy(activation = Loading)

      try {
        cardsMap.isNotEmpty() || throw IllegalStateException("Cards state not loaded")
        cardsMap.containsKey(code) || throw IllegalArgumentException("Card $code not found")

        val result: Result<Unit> = httpClient.execute(
          request = httpGet("${apiEndpoint}version?code=$code".toHttpUrl()),
          parse   = { json -> json["android"].string.toInt() }
        ).fold(
          onSuccess = {
            if (it > MIN_RESULT_INT)
              Result.success(Unit)
            else
              Result.failure(IllegalStateException("Failed to activate card!"))
          },
          onFailure = { Result.failure(it) }
        )

        val activatedCard = cardsMap[code]?.copy(isActivated = result.isSuccess) ?: throw IllegalArgumentException("Card $code not found")
        val newCards = cardsMap + (code to activatedCard)

        db.saveCards(newCards.values.toList())
        _data.value = _data.value.copy(activation = result.toState(), cards = Content(newCards))
      } catch (e: Exception) {
        _data.value = _data.value.copy(activation = Failure(e))
      }
    }
  }

  override fun resetActivate() {
    _data.value = _data.value.copy(activation = null)
  }

  override fun removeCard(code: String) {
    if (!cardsMap.containsKey(code))
      throw IllegalArgumentException("Card $code not found")

    val newCards = cardsMap - code
    db.saveCards(newCards.values.toList())
    _data.value = _data.value.copy(cards = Content(newCards))
  }
}
