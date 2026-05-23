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
