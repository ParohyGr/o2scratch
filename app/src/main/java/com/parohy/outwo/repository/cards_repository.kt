package com.parohy.outwo.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
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
  val isScratched: Boolean = false
)

interface CardsRepository {
  suspend fun loadCards()
  suspend fun generateCard()
  suspend fun scratchCard(code: String)
  fun activateCard(code: String)
  fun resetActivate()
  val data: StateFlow<CardRepositoryState>
}

private val Context.cardsPreferences get() = getSharedPreferences("cards", Context.MODE_PRIVATE)

data class CardRepositoryState(
  val cards: State<Throwable, Map<String, ScratchCard>>? = null,
  val activation: State<Throwable, Int>? = null
)

class CardsRepositoryImpl(private val context: Context): CardsRepository {
  private val _data: MutableStateFlow<CardRepositoryState> = MutableStateFlow(CardRepositoryState())
  override val data: StateFlow<CardRepositoryState> = _data

  private val cardsMap: Map<String, ScratchCard> get() = (_data.value.cards.valueOrNull ?: emptyMap())

  private val coroutineScope = CoroutineScope(Dispatchers.IO)
  private val httpClient by lazy {
    OkHttpClient.Builder()
      .addInterceptor(HttpLoggingInterceptor { Log.i("🪃OkHttp", it) }.apply { level = Level.BODY })
      .build() }

  override suspend fun loadCards() {
    if (_data.value.cards.isLoading) return

    withContext(Dispatchers.IO) {
      _data.value = _data.value.copy(cards = Loading)
      delay(2000)
      _data.value = _data.value.copy(Content(context.cardsPreferences.loadCards().associateBy(ScratchCard::code)))
    }
  }

  override suspend fun generateCard() {
    withContext(Dispatchers.IO) {
      delay(2000)
      val uuid = UUID.randomUUID().toString()
      val newCards = cardsMap + (uuid to ScratchCard(uuid))
      context.cardsPreferences.saveCards(newCards.values.toList())
      _data.value = _data.value.copy(cards = Content(newCards))
    }
  }

  override suspend fun scratchCard(code: String) {
    withContext(Dispatchers.IO) {
      delay(2000)
      val cardToScratch = cardsMap[code]?.copy(isScratched = true) ?: throw IllegalArgumentException("Card $code not found")
      val newCards = cardsMap + (code to cardToScratch)
      context.cardsPreferences.saveCards(newCards.values.toList())
      _data.value = _data.value.copy(cards = Content(newCards))
    }
  }

  override fun activateCard(code: String) {
    if (_data.value.activation.isLoading) return

    coroutineScope.launch {
      _data.value = _data.value.copy(activation = Loading)
      val result = httpClient.execute(
        request = httpGet("https://api.o2.sk/version?code=$code".toHttpUrl()),
        parse   = { json -> json["android"].string.toInt() }
      )
      _data.value = _data.value.copy(activation = result.toState())
    }
  }

  override fun resetActivate() {
    _data.value = _data.value.copy(activation = null)
  }
}

/*region DB*/
private fun SharedPreferences.loadCards(): Set<ScratchCard> {
  val cards = mutableListOf<ScratchCard>()
  val cardCodes = getStringSet("cards", emptySet()) ?: emptySet()
  Log.i("CardsRepositoryImpl", "loadCards: $cardCodes")
  cardCodes.forEach { cardsString -> cards += cardsString.split(";").let { ScratchCard(it.first(), it.last().toBoolean()) } }
  return cards.toSet()
}

private fun SharedPreferences.saveCards(cards: List<ScratchCard>) {
  val cardCodes = cards.map { "${it.code};${it.isScratched}" }.toSet()
  Log.i("CardsRepositoryImpl", "saveCards: $cardCodes")
  edit().putStringSet("cards", cardCodes).apply()
}
/*endregion*/