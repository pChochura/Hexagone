package com.pointlessgames.hexagone.achievements

import com.pointlessgames.hexagone.game.model.Perk
import kotlinx.coroutines.flow.SharedFlow

interface AchievementManager {
    fun unlockAchievement(achievement: GameAchievement)
    fun incrementAchievement(achievement: GameAchievement, amount: Int)
    fun showAchievements()
    
    // Lifetime tracking
    fun trackMerge()
    fun trackPerkUsed(perk: Perk)

    val unlockedAchievements: SharedFlow<GameAchievement>
}
