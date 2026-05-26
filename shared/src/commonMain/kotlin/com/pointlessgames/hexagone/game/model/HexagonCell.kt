package com.pointlessgames.hexagone.game.model

import androidx.compose.ui.graphics.Color

data class HexagonCell(
    val id: String,
    val x: Int,
    val y: Int,
    val value: Int
)

data class PreviewCell(
    val id: String,
    val x: Int,
    val y: Int,
    val value: Int,
    val rank: Int
)

data class MergeTransition(
    val targetX: Int,
    val targetY: Int,
    val steps: List<MergeStep>,
    val finalValue: Int,
    val totalCells: Int,
    val uniqueGroups: Int,
    val resultId: String
)

data class MergeStep(
    val mergingCells: List<HexagonCell>,
    val resultValue: Int
)

enum class Perk(
    val displayName: String,
    val description: String,
    val canSaveFromStuck: Boolean = true
) {
    ADVANCE_QUEUE("ADVANCE QUEUE", "Instantly spawn the next piece from your queue."),
    MOVE_TILE("MOVE TILE", "Select a tile and move it to any empty spot."),
    REMOVE_TILE("REMOVE TILE", "Select a tile and remove it from the board."),
    FUSION("FUSION", "Merge all surrounding tiles into a single superior tile."),
    SWAP_TILES("SWAP TILES", "Select two tiles to swap their positions."),
    CHAIN_MERGE("CHAIN MERGE", "Your next move will trigger chain reactions.", canSaveFromStuck = false),
    UNDO("UNDO", "Undo your last move.")
}

data class MergeHint(
    val x: Int,
    val y: Int,
    val weight: Float // 0.0 to 1.0
)

data class Particle(
    val id: Long,
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val life: Float,
    val size: Float
)

data class ScorePopup(
    val id: Long,
    val x: Float,
    val y: Float,
    val score: Int,
    val life: Float,
    val color: Color
)
