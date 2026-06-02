package com.pointlessgames.hexagone.achievements

import com.pointlessgames.hexagone.game.model.GameUiState
import com.pointlessgames.hexagone.game.model.Perk
import kotlinx.coroutines.flow.SharedFlow

data class AchievementStatus(
    val achievement: GameAchievement,
    val isUnlocked: Boolean,
    val currentProgress: Long = 0,
    val maxProgress: Long = 0
)

interface AchievementManager {
    fun unlockAchievement(achievement: GameAchievement)
    fun incrementAchievement(achievement: GameAchievement, amount: Int)
    fun showAchievements()
    
    // Session/State tracking for "Close Calls"
    fun updateSessionData(state: GameUiState)
    
    // Lifetime tracking
    fun trackMerge()
    fun trackPerkUsed(perk: Perk)
    fun trackReroll()

    val unlockedAchievements: SharedFlow<GameAchievement>
    suspend fun getAchievementsStatus(): List<AchievementStatus>
}
