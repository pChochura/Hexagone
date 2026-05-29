package com.pointlessgames.hexagone.game.model

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset

data class ConfettiPiece(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val rotation: Float,
    val rotationSpeed: Float,
    val color: Color,
    val life: Float,
    val size: Float,
    val flipSpeed: Float
)

class GhostAnimationState(
    val id: String,
    initialOffset: IntOffset,
) {
    val offset = Animatable(initialOffset, IntOffset.VectorConverter)
    val scale = Animatable(0f)
    val alpha = Animatable(1f)
}
