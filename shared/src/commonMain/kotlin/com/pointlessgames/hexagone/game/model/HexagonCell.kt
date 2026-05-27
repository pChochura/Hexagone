package com.pointlessgames.hexagone.game.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Immutable
interface GridPopup {
    val id: Long
    val x: Float
    val y: Float
    val gridX: Int
    val gridY: Int
}

@Immutable
@Serializable
data class HexagonCell(
    val id: String,
    val x: Int,
    val y: Int,
    val value: Int,
    val isTactical: Boolean = false
)

@Immutable
@Serializable
data class OnBoardPerk(
    val x: Int,
    val y: Int,
    val perk: Perk,
    val lifespan: Int
)

@Immutable
@Serializable
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

@Serializable
enum class Perk(
    val baseWeight: Int,
    val canSaveFromStuck: Boolean = true
) {
    UNDO(baseWeight = 100),
    MOVE_TILE(baseWeight = 80),
    REMOVE_TILE(baseWeight = 80),
    ADVANCE_QUEUE(baseWeight = 50),
    SWAP_TILES(baseWeight = 50),
    FUSION(baseWeight = 20),
    CHAIN_MERGE(baseWeight = 20, canSaveFromStuck = false);

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
    override val id: Long,
    override val x: Float,
    override val y: Float,
    override val gridX: Int,
    override val gridY: Int,
    val score: Int,
    val life: Float,
    val color: Color,
    val labelRes: StringResource? = null
) : GridPopup

@Immutable
data class PerkPopup(
    override val id: Long,
    override val x: Float,
    override val y: Float,
    override val gridX: Int,
    override val gridY: Int,
    val perk: Perk,
    val life: Float
) : GridPopup
