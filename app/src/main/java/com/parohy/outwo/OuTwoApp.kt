package com.parohy.outwo

import android.app.Application
import android.content.Context
import com.parohy.outwo.repository.*
import org.koin.core.context.startKoin
import org.koin.dsl.module

class OuTwoApp: Application(), CardPreferences {
  private val appModule by lazy {
    module {
      single<CardsRepository> { CardsRepositoryImpl(this@OuTwoApp) }
    }
  }

  private val cardsPreferences get() = getSharedPreferences("cards", Context.MODE_PRIVATE)

  override fun onCreate() {
    super.onCreate()
    startKoin { modules(appModule) }
  }

  override fun loadCards(): Set<ScratchCard> {
    val cards = mutableListOf<ScratchCard>()
    val cardCodes = cardsPreferences.getStringSet("cards", emptySet()) ?: emptySet()
    cardCodes.forEach { cardsString -> cards += cardsString.split(";").let { ScratchCard(it[0], it[1].toBoolean(), it[2].toBoolean()) } }
    return cards.toSet()
  }

  override fun saveCards(cards: List<ScratchCard>) {
    val cardCodes = cards.map { "${it.code};${it.isScratched};${it.isActivated}" }.toSet()
    cardsPreferences.edit().putStringSet("cards", cardCodes).apply()
  }
}