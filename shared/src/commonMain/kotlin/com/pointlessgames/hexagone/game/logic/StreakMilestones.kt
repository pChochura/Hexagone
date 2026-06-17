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
        return when (streak) {
            3 -> StreakReward(diamonds = 1)
            5 -> StreakReward(diamonds = 3)
            7 -> StreakReward(
                diamonds = 5,
                perkRewards = mapOf(PerkCategory.COMMON to 1),
            )

            14 -> StreakReward(
                diamonds = 10,
                perkRewards = mapOf(PerkCategory.COMMON to 2),
            )

            21 -> StreakReward(
                diamonds = 15,
                perkRewards = mapOf(PerkCategory.COMMON to 1, PerkCategory.RARE to 1),
            )

            30 -> StreakReward(
                diamonds = 25,
                perkRewards = mapOf(PerkCategory.RARE to 2),
            )

            60 -> StreakReward(
                diamonds = 50,
                perkRewards = mapOf(PerkCategory.RARE to 2, PerkCategory.COMMON to 2),
            )

            90 -> StreakReward(
                diamonds = 100,
                perkRewards = mapOf(PerkCategory.LEGENDARY to 1, PerkCategory.RARE to 1),
            )

            120 -> StreakReward(
                diamonds = 150,
                perkRewards = mapOf(
                    PerkCategory.LEGENDARY to 1,
                    PerkCategory.RARE to 2,
                    PerkCategory.COMMON to 2,
                ),
            )

            150 -> StreakReward(
                diamonds = 200,
                perkRewards = mapOf(
                    PerkCategory.LEGENDARY to 2,
                    PerkCategory.RARE to 1,
                ),
            )

            180 -> StreakReward(
                diamonds = 250,
                perkRewards = mapOf(
                    PerkCategory.LEGENDARY to 2,
                    PerkCategory.RARE to 2,
                    PerkCategory.COMMON to 2,
                ),
            )

            210 -> StreakReward(
                diamonds = 300,
                perkRewards = mapOf(PerkCategory.LEGENDARY to 3),
            )

            240 -> StreakReward(
                diamonds = 400,
                perkRewards = mapOf(
                    PerkCategory.LEGENDARY to 3,
                    PerkCategory.RARE to 3,
                    PerkCategory.COMMON to 3,
                ),
            )

            270 -> StreakReward(
                diamonds = 500,
                perkRewards = mapOf(PerkCategory.LEGENDARY to 3, PerkCategory.RARE to 5),
            )

            300 -> StreakReward(
                diamonds = 600,
                perkRewards = mapOf(PerkCategory.LEGENDARY to 4),
            )

            330 -> StreakReward(
                diamonds = 750,
                perkRewards = mapOf(
                    PerkCategory.LEGENDARY to 4,
                    PerkCategory.RARE to 4,
                    PerkCategory.COMMON to 4,
                ),
            )

            365 -> StreakReward(
                diamonds = 1000,
                perkRewards = mapOf(
                    PerkCategory.LEGENDARY to 5,
                    PerkCategory.RARE to 5,
                    PerkCategory.COMMON to 5,
                ),
            )

            else -> null
        }
    }
}
