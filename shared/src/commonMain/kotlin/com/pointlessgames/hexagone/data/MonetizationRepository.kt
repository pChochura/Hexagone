package com.pointlessgames.hexagone.data

import com.pointlessgames.hexagone.billing.BillingManager
import com.pointlessgames.hexagone.billing.PurchaseResult
import com.pointlessgames.hexagone.game.logic.PerkCategory
import com.pointlessgames.hexagone.game.logic.StreakMilestones
import com.pointlessgames.hexagone.game.model.Perk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlin.random.Random

class MonetizationRepository(
    private val billingManager: BillingManager,
    private val settingsRepository: SettingsRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        scope.launch {
            billingManager.purchaseEvents.collectLatest { result ->
                if (result is PurchaseResult.Success) {
                    handlePurchaseSuccess(result)
                }
            }
        }
    }

    private suspend fun handlePurchaseSuccess(result: PurchaseResult.Success) {
        when (result.productId) {
            "diamonds_100" -> addDiamonds(100)
            "diamonds_500" -> addDiamonds(500)
            "diamonds_1000" -> addDiamonds(1000)
            "perk_common_5" -> addRandomPerks(PerkCategory.COMMON, 5)
            "perk_rare_3" -> addRandomPerks(PerkCategory.RARE, 3)
            "perk_legendary_1" -> addRandomPerks(PerkCategory.LEGENDARY, 1)
        }
    }

    private suspend fun addDiamonds(amount: Int) {
        val current = settingsRepository.getDiamonds()
        settingsRepository.setDiamonds(current + amount)
    }

    private suspend fun addRandomPerks(category: PerkCategory, count: Int) {
        repeat(count) {
            val perk = StreakMilestones.getRandomPerkFromCategory(category, Random.Default)
            addPerkToBank(perk)
        }
    }

    suspend fun buyRandomPerk(category: PerkCategory): Perk? {
        val cost = when (category) {
            PerkCategory.COMMON -> 50
            PerkCategory.RARE -> 150
            PerkCategory.LEGENDARY -> 500
        }

        val currentDiamonds = settingsRepository.getDiamonds()
        if (currentDiamonds >= cost) {
            settingsRepository.setDiamonds(currentDiamonds - cost)
            val perk = StreakMilestones.getRandomPerkFromCategory(category, Random.Default)
            addPerkToBank(perk)
            return perk
        }
        return null
    }

    suspend fun addPerkToBank(perk: Perk) {
        val bankedJson = settingsRepository.getBankedPerks()
        val bankedPerks = bankedJson?.let {
            Json.decodeFromString<Map<Perk, Int>>(it)
        }?.toMutableMap() ?: mutableMapOf()

        bankedPerks[perk] = (bankedPerks[perk] ?: 0) + 1
        settingsRepository.setBankedPerks(Json.encodeToString(bankedPerks))
    }
}
