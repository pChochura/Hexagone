package com.pointlessgames.hexagone.game.logic

import com.pointlessgames.hexagone.game.model.HexagonCell
import com.pointlessgames.hexagone.game.model.MergeTransition
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.game.model.PreviewCell

object Scoring {
    const val MAX_COMBO_MULTIPLIER = 12
    const val REDEMPTION_COEFFICIENT = 0.25f

    fun calculateBarRaisedBonus(
        oldGrid: List<HexagonCell>,
        oldPreview: List<PreviewCell>,
        newGrid: List<HexagonCell>,
        newPreview: List<PreviewCell>,
    ): Int {
        val oldMin = (oldGrid.map { it.value } + oldPreview.map { it.value }).minOrNull() ?: return 0
        val newMin = (newGrid.map { it.value } + newPreview.map { it.value }).minOrNull() ?: return 0
        return if (newMin > oldMin) oldMin * 50 else 0
    }

    fun calculateSacrificeBonus(
        grid: List<HexagonCell>,
        removedCell: HexagonCell,
    ): Int {
        val isOnlyHighest = grid.count { it.value == removedCell.value } == 1 &&
                grid.all { it.value <= removedCell.value }

        if (!isOnlyHighest) return 0

        val sortedValues = grid.map { it.value }.distinct().sortedDescending()
        val secondHighest = if (sortedValues.size > 1) sortedValues[1] else 0
        val diff = removedCell.value - secondHighest

        return (removedCell.value * 25) + (diff * 50)
    }

    fun calculateRedemptionBonus(totalAddedScore: Int, lastMoveScore: Int?): Int {
        return if (lastMoveScore != null && totalAddedScore > lastMoveScore) {
            250 + ((totalAddedScore - lastMoveScore) * 0.5).toInt()
        } else {
            0
        }
    }

    fun calculateFinalCombo(merge: MergeTransition, currentCombo: Int, activePerk: Perk?): Int {
        val isPathMerge = merge.resultId.contains("path_merge")
        val isChainMerge = activePerk == Perk.CHAIN_MERGE

        if (isPathMerge || merge.uniqueGroups > 1) {
            return currentCombo
        }

        if (isChainMerge && currentCombo > 0) {
            return currentCombo
        }

        return 0
    }

    fun getNextStepCombo(currentCombo: Int, stepIndex: Int, isPathMerge: Boolean): Int {
        return if (stepIndex == 0 && isPathMerge) currentCombo + 1
        else if (stepIndex > 0) currentCombo + 1
        else currentCombo
    }

    fun getStepScore(baseScore: Int, nextCombo: Int): Int {
        val multiplier = (nextCombo + 1).coerceAtMost(MAX_COMBO_MULTIPLIER)
        return baseScore * multiplier
    }

    fun calculateFinalScore(
        merge: MergeTransition,
        grid: List<HexagonCell>,
        preview: List<PreviewCell>,
        initialCombo: Int,
        activePerk: Perk?,
        lastMoveScore: Int?,
    ): ScoreResult {
        var currentCombo = initialCombo
        var totalStepScore = 0
        val isPathMerge = merge.resultId.contains("path_merge")

        merge.steps.forEachIndexed { index, step ->
            currentCombo = getNextStepCombo(currentCombo, index, isPathMerge)
            totalStepScore += getStepScore(step.baseScore, currentCombo)
        }

        if (merge.steps.isEmpty()) {
            val nextCombo = getNextStepCombo(currentCombo, 0, isPathMerge)
            totalStepScore = getStepScore(merge.baseScore, nextCombo)
            currentCombo = nextCombo
        }

        val finalCombo = calculateFinalCombo(merge, currentCombo, activePerk)
        val comboMultiplier = (finalCombo + 1).coerceAtMost(MAX_COMBO_MULTIPLIER)

        val barRaisedBonus = when {
            merge.isRemoval -> {
                calculateBarRaisedBonus(
                    grid, preview,
                    grid.filterNot { it.x == merge.targetX && it.y == merge.targetY },
                    preview.filterNot { it.x == merge.targetX && it.y == merge.targetY }
                )
            }

            merge.resultId.contains("increment") -> {
                val nextGrid =
                    grid.map { if (it.x == merge.targetX && it.y == merge.targetY) it.copy(value = it.value + 1) else it }
                val nextPreview =
                    preview.map { if (it.x == merge.targetX && it.y == merge.targetY) it.copy(value = it.value + 1) else it }
                calculateBarRaisedBonus(grid, preview, nextGrid, nextPreview)
            }

            else -> 0
        }

        val sacrificeBonus = if (merge.isRemoval) {
            val cell = grid.find { it.x == merge.targetX && it.y == merge.targetY }
            if (cell != null) calculateSacrificeBonus(grid, cell) else 0
        } else 0

        val redemptionBonus = calculateRedemptionBonus(totalStepScore, lastMoveScore)
        val multipliedBonuses = (redemptionBonus + barRaisedBonus) * comboMultiplier

        return ScoreResult(
            totalScore = totalStepScore + multipliedBonuses + sacrificeBonus,
            stepScore = totalStepScore,
            bonusScore = multipliedBonuses,
            sacrificeBonus = sacrificeBonus,
            finalCombo = finalCombo,
            barRaisedBonus = barRaisedBonus,
            redemptionBonus = redemptionBonus
        )
    }
}

data class ScoreResult(
    val totalScore: Int,
    val stepScore: Int,
    val bonusScore: Int,
    val sacrificeBonus: Int,
    val finalCombo: Int,
    val barRaisedBonus: Int,
    val redemptionBonus: Int
)
