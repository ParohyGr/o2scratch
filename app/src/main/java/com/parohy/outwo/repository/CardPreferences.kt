package com.parohy.outwo.repository

interface CardPreferences {
  fun loadCards(): Set<ScratchCard>
  fun saveCards(cards: List<ScratchCard>)
}