package com.pointlessgames.hexagone.game

import androidx.compose.ui.graphics.Color
import com.pointlessgames.hexagone.game.model.GameEffect
import com.pointlessgames.hexagone.game.model.Particle
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.ui.theme.Colors
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.label_redemption
import hexagone.shared.generated.resources.label_tactician
import hexagone.shared.generated.resources.label_tactical_redemption
import hexagone.shared.generated.resources.label_bar_raised
import hexagone.shared.generated.resources.label_sacrifice
import hexagone.shared.generated.resources.label_janitor_plus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

internal class EffectDelegate(
    private val effects: MutableSharedFlow<GameEffect>,
    private val scope: CoroutineScope,
) {
    fun addParticles(newParticles: List<Particle>) = scope.launch {
        effects.emit(GameEffect.Particles(newParticles))
    }

    fun addMergeParticles(gridX: Int, gridY: Int, value: Int, isPerk: Boolean = false, intensity: Float = 1f) = scope.launch {
        effects.emit(GameEffect.MergeParticles(gridX, gridY, value, isPerk, intensity))
    }

    fun addScorePopup(
        gridX: Int,
        gridY: Int,
        score: Int,
        color: Color,
        labelRes: StringResource? = null,
    ) = scope.launch {
        effects.emit(GameEffect.ScorePopup(gridX, gridY, score, color, labelRes))
    }

    fun addPerkPopup(gridX: Int, gridY: Int, perk: Perk) = scope.launch {
        effects.emit(GameEffect.PerkPopup(gridX, gridY, perk))
    }

    fun handlePopups(
        targetX: Int,
        targetY: Int,
        totalScore: Int,
        isRedemption: Boolean,
        isBarRaised: Boolean,
        isSacrifice: Boolean = false,
        isTactical: Boolean = false,
    ) {
        scope.launch {
            val labelRes = when {
                isRedemption && isSacrifice -> Res.string.label_sacrifice
                isBarRaised && isSacrifice -> Res.string.label_janitor_plus
                isRedemption && isBarRaised -> Res.string.label_tactical_redemption
                isRedemption -> Res.string.label_redemption
                isSacrifice -> Res.string.label_sacrifice
                isBarRaised -> Res.string.label_bar_raised
                isTactical -> Res.string.label_tactician
                else -> null
            }
            val color = when {
                isRedemption -> Colors().yellow
                isBarRaised && isSacrifice -> Colors().gold
                isSacrifice -> Colors().pink
                isBarRaised -> Colors().skyBlue
                isTactical -> Colors().purple
                else -> Color.White
            }
            addScorePopup(targetX, targetY, totalScore, color, labelRes)
        }
    }
}
