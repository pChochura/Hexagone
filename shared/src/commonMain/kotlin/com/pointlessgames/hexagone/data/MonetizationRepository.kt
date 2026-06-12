package com.pointlessgames.hexagone.data

import com.pointlessgames.hexagone.billing.BillingManager
import com.pointlessgames.hexagone.billing.PurchaseResult
import com.pointlessgames.hexagone.game.logic.PerkCategory
import com.pointlessgames.hexagone.game.logic.StreakMilestones
import com.pointlessgames.hexagone.game.logic.StreakReward
import com.pointlessgames.hexagone.game.model.Perk
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlin.random.Random

class MonetizationRepository(
    private val billingManager: BillingManager,
    private val supabase: SupabaseClient
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
        // Handled by RevenueCat backend automatically on purchase
        billingManager.refreshBalance()
    }

    suspend fun buyPerkVoucher(category: PerkCategory): Boolean {
        val cost = when (category) {
            PerkCategory.COMMON -> 50
            PerkCategory.RARE -> 150
            PerkCategory.LEGENDARY -> 500
        }

        val currentDiamonds = billingManager.currencyBalances.value["diamonds"] ?: 0
        if (currentDiamonds >= cost) {
            val appUserId = billingManager.appUserId ?: return false
            
            try {
                val voucherKey = when (category) {
                    PerkCategory.COMMON -> "VCMN"
                    PerkCategory.RARE -> "VRARE"
                    PerkCategory.LEGENDARY -> "VLGD"
                }
                supabase.functions.invoke(
                    "adjust-economy",
                    AdjustEconomyRequest(
                        appUserId = appUserId,
                        adjustments = mapOf(
                            "diamonds" to -cost,
                            voucherKey to 1
                        )
                    )
                )

                billingManager.refreshBalance()
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }

    suspend fun usePerkVoucher(category: PerkCategory): Boolean {
        val voucherKey = when (category) {
            PerkCategory.COMMON -> "VCMN"
            PerkCategory.RARE -> "VRARE"
            PerkCategory.LEGENDARY -> "VLGD"
        }
        val currentVouchers = billingManager.currencyBalances.value[voucherKey] ?: 0
        
        if (currentVouchers > 0) {
            val appUserId = billingManager.appUserId ?: return false
            try {
                supabase.functions.invoke(
                    "adjust-economy",
                    AdjustEconomyRequest(
                        appUserId = appUserId,
                        adjustments = mapOf(voucherKey to -1)
                    )
                )
                billingManager.refreshBalance()
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }

    suspend fun awardStreakRewards(reward: StreakReward) {
        val appUserId = billingManager.appUserId ?: return
        
        val adjustments = mutableMapOf<String, Int>()
        if (reward.diamonds > 0) {
            adjustments["diamonds"] = reward.diamonds
        }
        reward.perkRewards.forEach { (category, count) ->
            val voucherKey = when (category) {
                PerkCategory.COMMON -> "VCMN"
                PerkCategory.RARE -> "VRARE"
                PerkCategory.LEGENDARY -> "VLGD"
            }
            adjustments[voucherKey] = count
        }

        if (adjustments.isNotEmpty()) {
            try {
                supabase.functions.invoke(
                    "adjust-economy",
                    AdjustEconomyRequest(
                        appUserId = appUserId,
                        adjustments = adjustments
                    )
                )
                billingManager.refreshBalance()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @Serializable
    private data class AdjustEconomyRequest(
        val appUserId: String,
        val adjustments: Map<String, Int>
    )
}
