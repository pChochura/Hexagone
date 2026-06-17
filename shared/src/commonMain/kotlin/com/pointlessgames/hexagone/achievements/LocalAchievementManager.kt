package com.pointlessgames.hexagone.achievements

import com.pointlessgames.hexagone.data.SettingsRepository
import com.pointlessgames.hexagone.game.model.GameUiState
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

    private var lastSessionState: GameUiState? = null

    override fun unlockAchievement(achievement: GameAchievement) {
        scope.launch {
            val unlocked = settingsRepository.getUnlockedAchievements()
            if (achievement.id !in unlocked) {
                settingsRepository.setAchievementUnlocked(achievement.id)
                _unlockedAchievements.emit(achievement)
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
                GameAchievement.CHANCE_TAKEN -> trackReroll()
                else -> {}
            }
        }
    }

    override fun showAchievements() {
        // Implementation for showing a list of achievements in Compose would go here
    }

    override fun updateSessionData(state: GameUiState) {
        lastSessionState = state
        scope.launch {
            settingsRepository.updateMaxComboLifetime(state.maxCombo)
            settingsRepository.updateHighestLevelLifetime(state.level)
            settingsRepository.updateMaxConsecutiveMergesLifetime(state.consecutiveMergesWithoutSpawn)
            settingsRepository.updateMaxTacticalMergesLifetime(state.tacticalMergesCount)
            settingsRepository.updateMaxCollectedPerksLifetime(state.collectedPerks.size)
            settingsRepository.updateMaxConsecutiveUndosLifetime(state.consecutiveUndos)
            settingsRepository.updateMaxTacticalGhostsLifetime(state.tacticalGhostsThisTurn)
            settingsRepository.updateMaxBarRaisedLifetime(state.barRaisedThisTurn)
            settingsRepository.updateHighestTileValueLifetime(state.highestValue)
        }
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

    override fun trackReroll() {
        scope.launch {
            settingsRepository.incrementRerollsLifetime()
            val total = settingsRepository.getRerollsLifetime()
            
            unlockAchievement(GameAchievement.CHANCE_TAKEN)
            if (total >= 15) {
                unlockAchievement(GameAchievement.GAMBLER)
            }
        }
    }

    override suspend fun getAchievementsStatus(): List<AchievementStatus> {
        val unlocked = settingsRepository.getUnlockedAchievements()
        val totalMerges = settingsRepository.getTotalMergesLifetime()
        val perksCollected = settingsRepository.getPerksCollectedLifetime()
        val gamesFinished = settingsRepository.getGamesFinishedLifetime()
        val rerolls = settingsRepository.getRerollsLifetime()
        val perksUsed = settingsRepository.getPerksUsedLifetime().size
        
        val maxCombo = settingsRepository.getMaxComboLifetime()
        val highestLevel = settingsRepository.getHighestLevelLifetime()
        val maxConsecutiveMerges = settingsRepository.getMaxConsecutiveMergesLifetime()
        val maxTacticalMerges = settingsRepository.getMaxTacticalMergesLifetime()
        val maxCollectedPerks = settingsRepository.getMaxCollectedPerksLifetime()
        val maxConsecutiveUndos = settingsRepository.getMaxConsecutiveUndosLifetime()
        val maxTacticalGhosts = settingsRepository.getMaxTacticalGhostsLifetime()
        val maxBarRaised = settingsRepository.getMaxBarRaisedLifetime()
        val highestTileValue = settingsRepository.getHighestTileValueLifetime()
        val highestScore = settingsRepository.getBestScore()

        return GameAchievement.entries.map { achievement ->
            val isUnlocked = achievement.id in unlocked
            val (current, max) = when (achievement) {
                GameAchievement.MARATHON -> totalMerges to 1000L
                GameAchievement.GAMBLER -> rerolls.toLong() to 15L
                GameAchievement.PERK_HUNTER -> perksCollected.toLong() to 1L
                GameAchievement.THE_JOURNEY_BEGINS -> gamesFinished.toLong() to 1L
                GameAchievement.PERK_COLLECTOR -> perksUsed.toLong() to Perk.entries.size.toLong()
                
                // Best-ever metrics for "Close Calls"
                GameAchievement.ASCENSION -> maxCombo.toLong() to 31L
                GameAchievement.MAXIMUM_OVERDRIVE -> maxCombo.toLong() to 21L
                GameAchievement.FEELING_THE_SURGE -> maxCombo.toLong() to 11L
                
                GameAchievement.EFFICIENCY_EXPERT -> maxConsecutiveMerges.toLong() to 10L
                GameAchievement.TACTICAL_GENIUS -> maxTacticalMerges.toLong() to 5L
                GameAchievement.DEEP_POCKETS -> maxCollectedPerks.toLong() to 10L
                GameAchievement.TIME_MACHINE -> maxConsecutiveUndos.toLong() to 3L
                GameAchievement.GHOST_PROTOCOL -> maxTacticalGhosts.toLong() to 3L
                GameAchievement.TRIPLE_THREAT -> maxBarRaised.toLong() to 3L
                
                GameAchievement.BEGINNER_LUCK -> highestScore.toLong() to 1000L
                GameAchievement.HEX_ARCHITECT -> highestScore.toLong() to 10000L
                GameAchievement.MILLIONAIRE_CLUB -> highestScore.toLong() to 100000L
                
                GameAchievement.A_NEW_BEGINNING -> highestLevel.toLong() to 5L
                GameAchievement.SEASONED_PLAYER -> highestLevel.toLong() to 15L
                GameAchievement.HEX_MASTER -> highestLevel.toLong() to 30L

                GameAchievement.THE_BIG_ONE -> highestTileValue.toLong() to 15L
                GameAchievement.BEYOND_LIMITS -> highestTileValue.toLong() to 20L

                else -> 0L to 0L
            }
            AchievementStatus(
                achievement = achievement,
                isUnlocked = isUnlocked,
                currentProgress = if (isUnlocked && max > 0) max else current,
                maxProgress = max
            )
        }
    }
}
