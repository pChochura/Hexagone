package com.pointlessgames.hexagone.game

import com.pointlessgames.hexagone.achievements.AchievementManager
import com.pointlessgames.hexagone.achievements.GameAchievement
import com.pointlessgames.hexagone.game.model.ComboTier
import com.pointlessgames.hexagone.game.model.GameUiState
import com.pointlessgames.hexagone.game.model.HexagonCell
import com.pointlessgames.hexagone.game.model.MergeTransition
import com.pointlessgames.hexagone.game.model.Perk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class AchievementDelegate(
    private val uiState: MutableStateFlow<GameUiState>,
    private val achievementManager: AchievementManager,
) {
    fun checkMergeAchievements(merge: MergeTransition) {
        // The whole gang: triggered when the player merges all 6 neighbors with the same value
        if (merge.totalCells >= 6 && merge.uniqueGroups == 1) {
            achievementManager.unlockAchievement(GameAchievement.THE_WHOLE_GANG)
        }

        if (merge.finalValue >= 15) {
            achievementManager.unlockAchievement(GameAchievement.THE_BIG_ONE)
        }
        if (merge.finalValue >= 20) {
            achievementManager.unlockAchievement(GameAchievement.BEYOND_LIMITS)
        }

        if (merge.steps.size >= 5) {
            achievementManager.unlockAchievement(GameAchievement.CHAIN_REACTION)
        }

        if (uiState.value.grid.size >= 19) {
            achievementManager.unlockAchievement(GameAchievement.LIVING_ON_THE_EDGE)
        }
    }

    fun checkComboBroken(oldCombo: Int, newCombo: Int) {
        if (oldCombo > 7 && newCombo == 0) {
            achievementManager.unlockAchievement(GameAchievement.COMBO_BREAKER)
        }
    }

    fun checkComboAchievements(finalCombo: Int) {
        if (finalCombo >= ComboTier.ZENITH.threshold) {
            achievementManager.unlockAchievement(GameAchievement.ASCENSION)
        } else if (finalCombo >= ComboTier.OVERDRIVE.threshold) {
            achievementManager.unlockAchievement(GameAchievement.MAXIMUM_OVERDRIVE)
        } else if (finalCombo >= ComboTier.SURGE.threshold) {
            achievementManager.unlockAchievement(GameAchievement.FEELING_THE_SURGE)
        }
    }

    fun checkLevelAchievements(level: Int) {
        if (level >= 30) {
            achievementManager.unlockAchievement(GameAchievement.HEX_MASTER)
        } else if (level >= 15) {
            achievementManager.unlockAchievement(GameAchievement.SEASONED_PLAYER)
        } else if (level >= 5) {
            achievementManager.unlockAchievement(GameAchievement.A_NEW_BEGINNING)
        }
    }

    fun checkScoreAchievements(score: Int) {
        if (score >= 100000) {
            achievementManager.unlockAchievement(GameAchievement.MILLIONAIRE_CLUB)
        } else if (score >= 10000) {
            achievementManager.unlockAchievement(GameAchievement.HEX_ARCHITECT)
        } else if (score >= 1000) {
            achievementManager.unlockAchievement(GameAchievement.BEGINNER_LUCK)
        }
    }

    fun checkPerkAchievements(perk: Perk, state: GameUiState) {
        achievementManager.unlockAchievement(GameAchievement.FIRST_AID)

        if (state.collectedPerks.size >= 10) {
            achievementManager.unlockAchievement(GameAchievement.DEEP_POCKETS)
        }

        if (perk == Perk.SKIP_SPAWN && state.grid.size >= 17) {
            achievementManager.unlockAchievement(GameAchievement.CALCULATED_RISK)
        }

        // Perk Collector check is now handled in AchievementManager.trackPerkUsed
        achievementManager.trackPerkUsed(perk)
    }

    fun onDoubleVisionOccurred() {
        achievementManager.unlockAchievement(GameAchievement.DOUBLE_VISION)
    }

    fun onRedemptionOccurred() {
        achievementManager.unlockAchievement(GameAchievement.REDEMPTION)
    }

    fun onPerkCollectedFromBoard() {
        achievementManager.unlockAchievement(GameAchievement.PERK_HUNTER)
        achievementManager.incrementAchievement(GameAchievement.PERK_HUNTER, 1)
    }

    fun onGameFinished() {
        achievementManager.unlockAchievement(GameAchievement.THE_JOURNEY_BEGINS)
        achievementManager.incrementAchievement(GameAchievement.THE_JOURNEY_BEGINS, 1)
    }

    fun onStuckResolvedWithPerk() {
        achievementManager.unlockAchievement(GameAchievement.MASTER_OF_FATE)
    }

    fun onUndoUsed() {
        uiState.update { 
            val newCount = it.consecutiveUndos + 1
            if (newCount >= 3) {
                achievementManager.unlockAchievement(GameAchievement.TIME_MACHINE)
            }
            it.copy(consecutiveUndos = newCount)
        }
    }

    fun onNonUndoAction() {
        uiState.update { it.copy(consecutiveUndos = 0) }
    }

    fun checkCleanse(gridBefore: List<HexagonCell>, gridAfter: List<HexagonCell>) {
        if (gridBefore.isNotEmpty() && gridAfter.isEmpty()) {
            achievementManager.unlockAchievement(GameAchievement.CLEANSE)
        }
    }

    fun onMergesIncremented(total: Int) {
        achievementManager.trackMerge()
        uiState.update { 
            val next = it.consecutiveMergesWithoutSpawn + 1
            if (next >= 10) {
                achievementManager.unlockAchievement(GameAchievement.EFFICIENCY_EXPERT)
            }
            it.copy(consecutiveMergesWithoutSpawn = next)
        }
    }

    fun onMergeDetails(isTactical: Boolean, isBarRaised: Boolean, isSacrifice: Boolean) {
        if (isBarRaised) achievementManager.unlockAchievement(GameAchievement.THE_JANITOR)
        if (isSacrifice) achievementManager.unlockAchievement(GameAchievement.SACRIFICE)
        if (isTactical) {
            uiState.update { 
                val next = it.tacticalMergesCount + 1
                if (next >= 5) {
                    achievementManager.unlockAchievement(GameAchievement.TACTICAL_GENIUS)
                }
                it.copy(tacticalMergesCount = next)
            }
        }
    }

    fun onSpawnOccurred() {
        uiState.update { it.copy(consecutiveMergesWithoutSpawn = 0) }
    }

    fun onRerollUsed() {
        achievementManager.trackReroll()
    }

    fun onRerollLegendary() {
        achievementManager.unlockAchievement(GameAchievement.HIGH_ROLLER)
    }

    fun onAdvancedJanitor() {
        achievementManager.unlockAchievement(GameAchievement.ADVANCED_JANITOR)
    }

    fun onPerkMissed() {
        achievementManager.unlockAchievement(GameAchievement.MISSED_OPPORTUNITY)
    }
}
