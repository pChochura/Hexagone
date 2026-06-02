package com.pointlessgames.hexagone.achievements

import com.pointlessgames.hexagone.data.SettingsRepository
import com.pointlessgames.hexagone.game.model.Perk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class LocalAchievementManager(
    private val settingsRepository: SettingsRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : AchievementManager {

    private val _unlockedAchievements = MutableSharedFlow<GameAchievement>()
    override val unlockedAchievements: SharedFlow<GameAchievement> = _unlockedAchievements.asSharedFlow()

    override fun unlockAchievement(achievement: GameAchievement) {
        scope.launch {
            val unlocked = settingsRepository.getUnlockedAchievements()
            if (achievement.id !in unlocked) {
                settingsRepository.setAchievementUnlocked(achievement.id)
                _unlockedAchievements.emit(achievement)
                println("Achievement Unlocked: ${achievement.id}")
            }
        }
    }

    override fun incrementAchievement(achievement: GameAchievement, amount: Int) {
        scope.launch {
            when (achievement) {
                GameAchievement.MARATHON -> trackMerge()
                GameAchievement.PERK_HUNTER -> {
                    settingsRepository.incrementPerksCollectedLifetime()
                    unlockAchievement(GameAchievement.PERK_HUNTER)
                }
                GameAchievement.THE_JOURNEY_BEGINS -> {
                    settingsRepository.incrementGamesFinishedLifetime()
                    unlockAchievement(GameAchievement.THE_JOURNEY_BEGINS)
                }
                else -> {}
            }
        }
    }

    override fun showAchievements() {
        // Implementation for showing a list of achievements in Compose would go here
    }

    override fun trackMerge() {
        scope.launch {
            settingsRepository.incrementTotalMergesLifetime()
            val total = settingsRepository.getTotalMergesLifetime()
            
            // Check milestones for incremental achievements
            if (total >= 1000) {
                unlockAchievement(GameAchievement.MARATHON)
            }
        }
    }

    override fun trackPerkUsed(perk: Perk) {
        scope.launch {
            settingsRepository.addPerkToLifetime(perk.name)
            val used = settingsRepository.getPerksUsedLifetime()

            if (used.size == Perk.entries.size) {
                unlockAchievement(GameAchievement.PERK_COLLECTOR)
            }
        }
    }
}
