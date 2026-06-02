package com.pointlessgames.hexagone.achievements

import com.pointlessgames.hexagone.game.model.Perk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class NoOpAchievementManager : AchievementManager {
    override fun unlockAchievement(achievement: GameAchievement) {}
    override fun incrementAchievement(achievement: GameAchievement, amount: Int) {}
    override fun showAchievements() {}
    override fun trackMerge() {}
    override fun trackPerkUsed(perk: Perk) {}

    override val unlockedAchievements: SharedFlow<GameAchievement> = MutableSharedFlow<GameAchievement>().asSharedFlow()
}
