package com.pointlessgames.hexagone.game

import com.pointlessgames.hexagone.achievements.AchievementManager
import com.pointlessgames.hexagone.achievements.GameAchievement
import com.pointlessgames.hexagone.game.logic.GameEngine
import com.pointlessgames.hexagone.game.logic.PatternRecognitionEngine
import com.pointlessgames.hexagone.game.model.ComboTier
import com.pointlessgames.hexagone.game.model.GameUiState
import com.pointlessgames.hexagone.game.model.HexagonCell
import com.pointlessgames.hexagone.game.model.MergeTransition
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.game.model.PotentialMerge
import com.pointlessgames.hexagone.game.model.PreviewCell
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class AchievementDelegate(
    private val uiState: MutableStateFlow<GameUiState>,
    private val achievementManager: AchievementManager,
) {
    fun checkMergeAchievements(merge: MergeTransition, engine: GameEngine, totalScore: Int) {
        // The whole gang: triggered when the player merges all 6 neighbors with the same value
        // into an empty center.
        if (merge.totalCells == 6 && merge.uniqueGroups == 1) {
            achievementManager.unlockAchievement(GameAchievement.THE_WHOLE_GANG)
            if (merge.steps.any { it.mergingCells.any { it.isMimic } }) {
                achievementManager.unlockAchievement(GameAchievement.PERFECT_FIT)
            }
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

        val previousGridSize = uiState.value.grid.size - 1 + merge.totalCells
        if (previousGridSize >= (engine.columns * engine.rows) - 3) {
            achievementManager.unlockAchievement(GameAchievement.LIVING_ON_THE_EDGE)
        }

        if (merge.resultId.contains("path_merge") && merge.totalCells >= 10) {
            achievementManager.unlockAchievement(GameAchievement.SNAKE_CHARMER)
            if (merge.steps.any { it.mergingCells.any { it.isMimic } }) {
                achievementManager.unlockAchievement(GameAchievement.PERFECT_FIT)
            }
        }

        if (merge.isTactical && totalScore > 1000) {
            achievementManager.unlockAchievement(GameAchievement.TACTICAL_GENIUS_ELITE)
        }

        if (merge.uniqueGroups == 3 && !merge.isPerkAssisted) {
            achievementManager.unlockAchievement(GameAchievement.ALL_AROUND)
        }

        if (merge.isMimicOnly) {
            achievementManager.unlockAchievement(GameAchievement.DOPPELGANGER)
        }

        checkPatternAchievements(uiState.value.grid, uiState.value.preview, engine)
    }

    fun checkComboBroken(oldCombo: Int, newCombo: Int) {
        if (oldCombo > 7 && newCombo == 0) {
            achievementManager.unlockAchievement(GameAchievement.COMBO_BREAKER)
        }
    }

    fun checkComboAchievements(finalCombo: Int) {
        if (finalCombo > 0) {
            uiState.update { it.copy(comboTriggeredInSession = true) }
        }

        val effectiveCombo = finalCombo + 1
        if (effectiveCombo >= ComboTier.ZENITH.threshold) {
            achievementManager.unlockAchievement(GameAchievement.ASCENSION)
        }
        if (effectiveCombo >= ComboTier.OVERDRIVE.threshold) {
            achievementManager.unlockAchievement(GameAchievement.MAXIMUM_OVERDRIVE)
        }
        if (effectiveCombo >= ComboTier.SURGE.threshold) {
            achievementManager.unlockAchievement(GameAchievement.FEELING_THE_SURGE)
        }
    }

    fun checkLevelAchievements(level: Int) {
        if (level >= 30) {
            achievementManager.unlockAchievement(GameAchievement.HEX_MASTER)
            if (!uiState.value.ghostPerkUsedInSession) {
                achievementManager.unlockAchievement(GameAchievement.SOLID_GROUND)
            }
        }
        if (level >= 15) {
            achievementManager.unlockAchievement(GameAchievement.SEASONED_PLAYER)
        }

        if (level >= 10 && !uiState.value.perkUsedInSession) {
            achievementManager.unlockAchievement(GameAchievement.ASCETIC)
        }

        if (level >= 5) {
            achievementManager.unlockAchievement(GameAchievement.A_NEW_BEGINNING)
            if (!uiState.value.comboTriggeredInSession) {
                achievementManager.unlockAchievement(GameAchievement.PACIFIST)
            }
        }

        if (level >= 20 && !uiState.value.undoUsedInSession) {
            achievementManager.unlockAchievement(GameAchievement.ZEN_MASTER)
        }
    }

    fun checkScoreAchievements(score: Int) {
        if (score >= 100000) {
            achievementManager.unlockAchievement(GameAchievement.MILLIONAIRE_CLUB)
        }
        if (score >= 10000) {
            achievementManager.unlockAchievement(GameAchievement.HEX_ARCHITECT)
        }
        if (score >= 1000) {
            achievementManager.unlockAchievement(GameAchievement.BEGINNER_LUCK)
        }
    }

    fun unlockDeepPockets() {
        achievementManager.unlockAchievement(GameAchievement.DEEP_POCKETS)
    }

    fun checkPerkAchievements(perk: Perk, state: GameUiState, isTargetGhost: Boolean = false) {
        uiState.update { it.copy(perkUsedInSession = true) }
        achievementManager.unlockAchievement(GameAchievement.FIRST_AID)

        if (isTargetGhost) {
            handleGhostPerkTrigger(perk)
        }

        if (perk == Perk.SKIP_SPAWN && state.grid.size >= 17) {
            achievementManager.unlockAchievement(GameAchievement.CALCULATED_RISK)
        }

        if (perk == Perk.MIMIC) {
            achievementManager.unlockAchievement(GameAchievement.MIMICRY_101)
            if (isTargetGhost) {
                achievementManager.unlockAchievement(GameAchievement.SPECTRAL_MIMIC)
            }
        }

        // Perk Collector check is now handled in AchievementManager.trackPerkUsed
        achievementManager.trackPerkUsed(perk)
    }

    private fun handleGhostPerkTrigger(perk: Perk) {
        uiState.update { it.copy(ghostPerkUsedInSession = true) }
        when (perk) {
            Perk.MOVE_TILE -> achievementManager.unlockAchievement(GameAchievement.PHANTOM_MOVE)
            Perk.DUPLICATE_TILE -> achievementManager.unlockAchievement(GameAchievement.SPECTRAL_ECHO)
            Perk.REMOVE_TILE -> achievementManager.unlockAchievement(GameAchievement.CLEAN_SWEEP)
            Perk.SWAP_TILES -> achievementManager.unlockAchievement(GameAchievement.POLTERGEIST)
            Perk.INCREMENT_TILE -> achievementManager.unlockAchievement(GameAchievement.GHOSTLY_ENHANCEMENT)
            else -> {}
        }
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
            it.copy(consecutiveUndos = newCount, undoUsedInSession = true)
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

    fun onMergesIncremented(groups: Int) {
        achievementManager.trackMerge()
        uiState.update {
            val count = maxOf(1, groups)
            val next = it.consecutiveMergesWithoutSpawn + count
            if (next >= 10) {
                achievementManager.unlockAchievement(GameAchievement.EFFICIENCY_EXPERT)
            }
            it.copy(consecutiveMergesWithoutSpawn = next)
        }
    }

    fun onMergeDetails(isTactical: Boolean, isBarRaised: Boolean, isSacrifice: Boolean) {
        if (isBarRaised) {
            achievementManager.unlockAchievement(GameAchievement.THE_JANITOR)
            uiState.update {
                val next = it.barRaisedThisTurn + 1
                if (next >= 3) {
                    achievementManager.unlockAchievement(GameAchievement.TRIPLE_THREAT)
                }
                it.copy(barRaisedThisTurn = next)
            }
        }
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
        uiState.update {
            it.copy(
                consecutiveMergesWithoutSpawn = 0,
                barRaisedThisTurn = 0,
                tacticalGhostsThisTurn = 0,
            )
        }
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

    fun onGhostMarkedTactical() {
        uiState.update {
            val next = it.tacticalGhostsThisTurn + 1
            if (next >= 3) {
                achievementManager.unlockAchievement(GameAchievement.GHOST_PROTOCOL)
            }
            it.copy(tacticalGhostsThisTurn = next)
        }
    }

    fun onPossession() {
        achievementManager.unlockAchievement(GameAchievement.POSSESSION)
    }

    fun checkPatternAchievements(
        grid: List<HexagonCell>,
        previews: List<PreviewCell>,
        engine: GameEngine,
    ) {
        val patternResults = listOf(
            PatternRecognitionEngine.checkRingOfFire(grid, engine) to GameAchievement.RING_OF_FIRE,
            PatternRecognitionEngine.checkGreatWall(grid, engine) to GameAchievement.GREAT_WALL,
            PatternRecognitionEngine.checkTwinPeaks(grid, engine) to GameAchievement.TWIN_PEAKS,
            PatternRecognitionEngine.checkThePrism(grid) to GameAchievement.THE_PRISM,
            PatternRecognitionEngine.checkTheMedium(grid, previews, engine) to GameAchievement.THE_MEDIUM,
        )

        patternResults.forEach { (res, achievement) ->
            if (res.success) {
                achievementManager.unlockAchievement(achievement)
                if (res.containsMimic) {
                    achievementManager.unlockAchievement(GameAchievement.PERFECT_FIT)
                }
            }
        }

        if (PatternRecognitionEngine.checkMirrorImage(grid, engine)) {
            achievementManager.unlockAchievement(GameAchievement.MIRROR_IMAGE)
        }

        if (PatternRecognitionEngine.checkQuadruplets(previews)) {
            achievementManager.unlockAchievement(GameAchievement.QUADRUPLETS)
        }
    }

    fun checkArchitectsDream(
        grid: List<HexagonCell>,
        potentialMerges: Map<Pair<Int, Int>, PotentialMerge>,
    ) {
        if (PatternRecognitionEngine.checkArchitectsDream(grid, potentialMerges)) {
            achievementManager.unlockAchievement(GameAchievement.ARCHITECTS_DREAM)
            if (grid.any { it.isMimic }) {
                achievementManager.unlockAchievement(GameAchievement.PERFECT_FIT)
            }
        }
    }
}
