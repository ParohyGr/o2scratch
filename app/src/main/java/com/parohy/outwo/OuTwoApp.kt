package com.parohy.outwo

import android.app.Application
import com.parohy.outwo.repository.CardsRepository
import com.parohy.outwo.repository.CardsRepositoryImpl
import org.koin.core.context.startKoin
import org.koin.dsl.module

class OuTwoApp: Application() {
  private val appModule by lazy {
    module {
      single<CardsRepository> { CardsRepositoryImpl(applicationContext) }
    }
  }

  override fun onCreate() {
    super.onCreate()
    startKoin { modules(appModule) }
  }
}