package com.pointlessgames.hexagone.game

import com.pointlessgames.hexagone.game.logic.PatternRecognitionEngine
import com.pointlessgames.hexagone.game.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class ChallengeDelegate(
    private val uiState: MutableStateFlow<GameUiState>,
    private val onChallengeComplete: (DailyChallenge) -> Unit
) {
    fun onMerge(merge: MergeTransition) {
        val state = uiState.value
        updateChallenges { progress ->
            if (progress.isCompleted) return@updateChallenges progress
            
            val challenge = progress.challenge
            when (challenge.goal) {
                ChallengeGoal.MERGE_COUNT -> progress.copy(progress = progress.progress + 1)
                ChallengeGoal.PIECE_VALUE_REACHED -> if (merge.finalValue >= challenge.target) {
                    progress.copy(progress = challenge.target)
                } else progress
                ChallengeGoal.TACTICAL_MERGES -> if (merge.isTactical) {
                    progress.copy(progress = progress.progress + 1)
                } else progress
                ChallengeGoal.ELITE_SACRIFICE -> {
                    val maxVal = state.grid.maxOfOrNull { it.value } ?: 0
                    val secondMaxVal = state.grid.filter { it.value < maxVal }.maxOfOrNull { it.value } ?: 0
                    if (merge.isRemoval && merge.steps.first().mergingCells.any { it.value == maxVal } && (maxVal - secondMaxVal) >= 5) {
                        progress.copy(progress = challenge.target)
                    } else progress
                }
                ChallengeGoal.PATH_MERGE_COUNT -> if (merge.resultId.contains("path_merge") && merge.totalCells >= challenge.target) {
                    progress.copy(progress = challenge.target)
                } else progress
                ChallengeGoal.FROZEN_RECOVERY -> {
                    val wasThawed = merge.steps.first().mergingCells.any { it.id in state.thawedIds }
                    if (wasThawed) progress.copy(progress = challenge.target) else progress
                }
                else -> progress
            }
        }
        
        uiState.update { it.copy(thawedIds = emptySet()) }
    }

    fun onLevelUp(newLevel: Int) {
        val state = uiState.value
        updateChallenges { progress ->
            if (progress.isCompleted) return@updateChallenges progress
            
            val challenge = progress.challenge
            when (challenge.goal) {
                ChallengeGoal.LEVEL_REACHED -> if (newLevel >= challenge.target) {
                    progress.copy(progress = challenge.target)
                } else progress
                ChallengeGoal.PERK_RESTRICTED_LEVEL -> {
                    val perkUsed = state.perksUsedTracking[challenge.restrictedPerk] ?: 0
                    if (newLevel >= challenge.target && perkUsed == 0) {
                        progress.copy(progress = challenge.target)
                    } else progress
                }
                ChallengeGoal.FRUGAL_SURVIVOR -> {
                    if (newLevel >= challenge.target && state.collectedPerks.size >= 8) {
                        progress.copy(progress = challenge.target)
                    } else progress
                }
                else -> progress
            }
        }
    }

    fun onCombo(combo: Int) {
        updateChallenges { progress ->
            if (progress.isCompleted) return@updateChallenges progress
            
            val challenge = progress.challenge
            if (challenge.goal == ChallengeGoal.COMBO_REACHED && (combo + 1) >= challenge.target) {
                progress.copy(progress = challenge.target)
            } else progress
        }
    }

    fun onScoreChanged(newScore: Int) {
        updateChallenges { progress ->
            if (progress.isCompleted) return@updateChallenges progress
            
            val challenge = progress.challenge
            if (challenge.goal == ChallengeGoal.SCORE_REACHED && newScore >= challenge.target) {
                progress.copy(progress = challenge.target)
            } else progress
        }
    }

    fun onPerkUsed(perk: Perk) {
        uiState.update { it.copy(movesWithoutPerk = 0) }
    }

    fun onMovePerformed() {
        uiState.update { it.copy(movesWithoutPerk = it.movesWithoutPerk + 1) }
        val movesWithoutPerk = uiState.value.movesWithoutPerk
        
        updateChallenges { progress ->
            if (progress.isCompleted) return@updateChallenges progress
            val challenge = progress.challenge
            if (challenge.goal == ChallengeGoal.MOVES_WITHOUT_PERK && movesWithoutPerk >= challenge.target) {
                progress.copy(progress = challenge.target)
            } else progress
        }
    }

    fun onReroll(options: List<Perk>) {
        val hasLegendary = options.any { it.isLegendary }
        if (hasLegendary) {
            updateChallenges { progress ->
                if (progress.isCompleted) return@updateChallenges progress
                if (progress.challenge.goal == ChallengeGoal.LEGENDARY_GAMBLE) {
                    progress.copy(progress = progress.challenge.target)
                } else progress
            }
        }
    }

    fun checkBoardState(engine: com.pointlessgames.hexagone.game.logic.GameEngine) {
        val state = uiState.value
        val grid = state.grid
        
        updateChallenges { progress ->
            if (progress.isCompleted) return@updateChallenges progress
            val challenge = progress.challenge
            when (challenge.goal) {
                ChallengeGoal.GEOMETRIC_PATTERN -> {
                    val patternMet = when (challenge.patternId) {
                        "ring_of_fire" -> PatternRecognitionEngine.checkRingOfFire(grid, engine)
                        "great_wall" -> PatternRecognitionEngine.checkGreatWall(grid, engine)
                        "twin_peaks" -> PatternRecognitionEngine.checkTwinPeaks(grid, engine)
                        "the_prism" -> PatternRecognitionEngine.checkThePrism(grid)
                        else -> false
                    }
                    if (patternMet) progress.copy(progress = challenge.target) else progress
                }
                ChallengeGoal.GHOST_HORDE -> {
                    val grouped = state.preview.groupBy { it.value }
                    val maxSame = grouped.values.maxOfOrNull { it.size } ?: 0
                    if (maxSame >= challenge.target) progress.copy(progress = challenge.target) else progress
                }
                ChallengeGoal.DIVERSITY_STREAK -> {
                    val values = grid.map { it.value }.toSet()
                    val hasAll = (1..7).all { it in values }
                    if (hasAll) progress.copy(progress = challenge.target) else progress
                }
                else -> progress
            }
        }
    }

    fun onSpawnOccurred(combo: Int) {
        val targetThreshold = 5
        if (combo >= targetThreshold) {
            uiState.update { it.copy(comboMaintenanceTurns = it.comboMaintenanceTurns + 1) }
        } else {
            uiState.update { it.copy(comboMaintenanceTurns = 0) }
        }
        
        val maintenance = uiState.value.comboMaintenanceTurns
        updateChallenges { progress ->
            if (progress.isCompleted) return@updateChallenges progress
            if (progress.challenge.goal == ChallengeGoal.COMBO_MAINTENANCE && maintenance >= progress.challenge.target) {
                progress.copy(progress = progress.challenge.target)
            } else progress
        }
    }

    private fun updateChallenges(transform: (DailyChallengeProgress) -> DailyChallengeProgress) {
        val completedChallenges = mutableListOf<DailyChallenge>()
        
        uiState.update { state ->
            val updatedList = state.dailyChallenges.map { progress ->
                val next = transform(progress)
                val wasCompleted = progress.isCompleted
                val isNowCompleted = next.progress >= next.challenge.target
                
                if (!wasCompleted && isNowCompleted) {
                    val finalProgress = next.copy(isCompleted = true, progress = next.challenge.target)
                    completedChallenges.add(finalProgress.challenge)
                    finalProgress
                } else {
                    next
                }
            }
            state.copy(dailyChallenges = updatedList)
        }
        
        completedChallenges.forEach { onChallengeComplete(it) }
    }
}
