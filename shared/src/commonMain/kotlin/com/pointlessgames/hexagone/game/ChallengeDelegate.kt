package com.pointlessgames.hexagone.game

import com.pointlessgames.hexagone.game.model.ChallengeGoal
import com.pointlessgames.hexagone.game.model.DailyChallenge
import com.pointlessgames.hexagone.game.model.DailyChallengeProgress
import com.pointlessgames.hexagone.game.model.GameUiState
import com.pointlessgames.hexagone.game.model.MergeTransition
import com.pointlessgames.hexagone.game.model.Perk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class ChallengeDelegate(
    private val uiState: MutableStateFlow<GameUiState>,
    private val onChallengeComplete: (DailyChallenge) -> Unit
) {
    fun onMerge(merge: MergeTransition) {
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
                else -> progress
            }
        }
    }

    fun onLevelUp(newLevel: Int) {
        updateChallenges { progress ->
            if (progress.isCompleted) return@updateChallenges progress
            
            val challenge = progress.challenge
            if (challenge.goal == ChallengeGoal.LEVEL_REACHED && newLevel >= challenge.target) {
                progress.copy(progress = challenge.target)
            } else progress
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
        // Reserved for future goals
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
