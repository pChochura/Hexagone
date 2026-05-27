package com.pointlessgames.hexagone.game.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class HexagonCell(
    val id: String,
    val x: Int,
    val y: Int,
    val value: Int,
    val isTactical: Boolean = false
)

@Immutable
data class OnBoardPerk(
    val x: Int,
    val y: Int,
    val perk: Perk,
    val lifespan: Int
)

@Immutable
data class PreviewCell(
    val id: String,
    val x: Int,
    val y: Int,
    val value: Int,
    val rank: Int,
    val isTactical: Boolean = false
)

@Immutable
data class MergeTransition(
    val targetX: Int,
    val targetY: Int,
    val steps: List<MergeStep>,
    val finalValue: Int,
    val totalCells: Int,
    val uniqueGroups: Int,
    val baseScore: Int,
    val resultId: String,
    val isTactical: Boolean = false
)

@Immutable
data class MergeStep(
    val mergingCells: List<HexagonCell>,
    val resultValue: Int
)

enum class Perk(
    val displayName: String,
    val description: String,
    val baseWeight: Int,
    val canSaveFromStuck: Boolean = true
) {
    UNDO("UNDO", "Undo your last move.", baseWeight = 100),
    MOVE_TILE("MOVE TILE", "Select a tile and move it to any empty spot.", baseWeight = 80),
    REMOVE_TILE("REMOVE TILE", "Select a tile and remove it from the board.", baseWeight = 80),
    ADVANCE_QUEUE("ADVANCE QUEUE", "Instantly spawn the next piece from your queue.", baseWeight = 50),
    SWAP_TILES("SWAP TILES", "Select two tiles to swap their positions.", baseWeight = 50),
    FUSION("FUSION", "Merge all surrounding tiles into a single superior tile.", baseWeight = 20),
    CHAIN_MERGE("CHAIN MERGE", "Your next move will trigger chain reactions.", baseWeight = 20, canSaveFromStuck = false);

    val isLegendary: Boolean get() = baseWeight <= 20
}

@Immutable
data class MergeHint(
    val x: Int,
    val y: Int,
    val weight: Float // 0.0 to 1.0
)

@Immutable
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

@Immutable
data class ScorePopup(
    val id: Long,
    val x: Float,
    val y: Float,
    val score: Int,
    val life: Float,
    val color: Color,
    val label: String? = null
)

@Immutable
data class PerkPopup(
    val id: Long,
    val x: Float,
    val y: Float,
    val perk: Perk,
    val life: Float
)
