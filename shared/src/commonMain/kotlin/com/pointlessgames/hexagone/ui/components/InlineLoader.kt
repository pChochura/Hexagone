package com.pointlessgames.hexagone.ui.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.EaseInOutElastic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import com.pointlessgames.hexagone.ui.theme.DefaultIconsSize

private const val ANIMATION_DURATION = 500

@Composable
internal fun InlineLoader(
    size: Dp = DefaultIconsSize.current.extraLarge,
    animationDuration: Int = ANIMATION_DURATION,
) {
    val transition = rememberInfiniteTransition("InlineLoader")
    val radius by transition.animateFloat(
        initialValue = 100f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = animationDuration,
                easing = EaseInOutElastic,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 90f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = animationDuration,
                easing = EaseInOutElastic,
            ),
            repeatMode = RepeatMode.Reverse,
        )
    )
    val color by transition.animateColor(
        initialValue = Color(192, 133, 82),
        targetValue = Color(137, 87, 55),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = animationDuration,
                easing = EaseInOutElastic,
            ),
            repeatMode = RepeatMode.Reverse,
        )
    )

    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer { rotationZ = rotation }
            .drawWithCache {
                val cornerRadius = CornerRadius(radius, radius)
                onDrawBehind {
                    drawRoundRect(
                        color = color,
                        cornerRadius = cornerRadius,
                    )
                }
            }
    )
}
