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
    val mergingCells: List<HexagonCell>,
    val newValue: Int
)

enum class Perk(val displayName: String, val description: String) {
    ADVANCE_QUEUE("ADVANCE QUEUE", "Instantly spawn the next piece from your queue."),
    MOVE_TILE("MOVE TILE", "Select a tile and move it to any empty spot."),
    REMOVE_TILE("REMOVE TILE", "Select a tile and remove it from the board."),
    FUSION("FUSION", "Merge all surrounding tiles into a single superior tile.")
}

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
