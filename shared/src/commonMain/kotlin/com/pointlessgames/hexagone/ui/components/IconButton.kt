package com.pointlessgames.hexagone.ui.components

import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import com.pointlessgames.hexagone.ui.TiltedRoundedCornersShape
import com.pointlessgames.hexagone.ui.theme.DefaultCornerRadius
import com.pointlessgames.hexagone.ui.theme.DefaultIconsSize
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun IconButton(
    isEnabled: Boolean,
    iconRes: DrawableResource,
    contentDescription: StringResource,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    isPulsating: Boolean = false,
    tint: Color = LocalContentColor.current,
    position: Position = Position.ABOVE,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsatingEffect")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = EaseOut),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pulseScale",
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = EaseOut),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pulseAlpha",
    )

    ShapeButton(
        modifier = Modifier.drawBehind {
            if (!isPulsating) return@drawBehind

            drawCircle(
                color = tint.copy(alpha = pulseAlpha),
                radius = size.minDimension * pulseScale * 0.5f,
                center = center,
            )
        },
        size = DefaultIconsSize.current.large,
        iconSize = DefaultIconsSize.current.medium,
        icon = iconRes,
        contentDescription = contentDescription,
        defaultShape = TiltedRoundedCornersShape(0f, DefaultCornerRadius.current.full),
        pressedShape = TiltedRoundedCornersShape(0f, DefaultCornerRadius.current.full),
        defaultBackgroundColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0f),
        pressedBackgroundColor = MaterialTheme.colorScheme.tertiary,
        contentColor = tint,
        isLoading = isLoading,
        isEnabled = isEnabled,
        dragForce = 0.01f,
        tooltipPosition = position,
        onClick = onClick,
    )
}
