package com.pointlessgames.hexagone.game.logic

import com.pointlessgames.hexagone.game.model.Perk

data class StreakReward(
    val diamonds: Int = 0,
    val perkRewards: Map<PerkCategory, Int> = emptyMap(),
)

enum class PerkCategory {
    COMMON, RARE, LEGENDARY
}

object StreakMilestones {
    fun getRewardForStreak(streak: Int): StreakReward? {
        if (streak <= 0) return null

        val diamonds = 5 + (streak / 2).coerceAtMost(45) // max 50
        
        val commonVouchers = 1 + (streak / 7)
        val rareVouchers = if (streak >= 7) 1 + (streak / 14) else 0
        val legendaryVouchers = if (streak >= 30) 1 + (streak / 30) else 0

        val perkRewards = mutableMapOf<PerkCategory, Int>()
        if (commonVouchers > 0) perkRewards[PerkCategory.COMMON] = commonVouchers
        if (rareVouchers > 0) perkRewards[PerkCategory.RARE] = rareVouchers
        if (legendaryVouchers > 0) perkRewards[PerkCategory.LEGENDARY] = legendaryVouchers

        return StreakReward(
            diamonds = diamonds,
            perkRewards = perkRewards
        )
    }
}
