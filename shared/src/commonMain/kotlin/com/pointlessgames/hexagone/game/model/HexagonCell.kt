package com.pointlessgames.hexagone.game.model

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
