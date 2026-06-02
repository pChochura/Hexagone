package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Shape
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.spacing
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun WavyProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    waveIntensity: Float = 0f,
    showContainer: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = Color.White.copy(alpha = 0.05f),
    shape: Shape = RoundedCornerShape(MaterialTheme.cornerRadius.large),
    isWavy: Boolean = true
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "progress_animation",
    )

    val waveOffset = remember { Animatable(0f) }
    LaunchedEffect(isWavy) {
        if (!isWavy) return@LaunchedEffect
        var lastTimeNanos = withFrameNanos { it }
        while (true) {
            val currentTimeNanos = withFrameNanos { it }
            val dt = (currentTimeNanos - lastTimeNanos) / 1_000_000_000f
            lastTimeNanos = currentTimeNanos
            val baseSpeed = PI.toFloat() / 2f
            val speed = baseSpeed * (1f + waveIntensity * 4f)
            val delta = dt * speed
            waveOffset.snapTo((waveOffset.value + delta) % (2 * PI.toFloat()))
        }
    }

    val colorScheme = MaterialTheme.colorScheme
    val spacing = MaterialTheme.spacing

    val finalContainerColor = if (showContainer) containerColor else Color.Transparent
    val finalBorderColor = if (showContainer) borderColor else Color.Transparent

    Box(
        modifier = modifier
            .background(finalContainerColor, shape)
            .border(spacing.extraTiny, finalBorderColor, shape)
            .clip(shape)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width * animatedProgress
            val height = size.height

            if (width > 0) {
                val currentWaveOffset = waveOffset.value
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(width, 0f)

                    val waveAmplitude = if (isWavy)
                        (spacing.extraSmall + spacing.semiMedium * waveIntensity).toPx()
                    else 0f
                    val wavePeriod = height * 0.8f

                    val steps = 30
                    for (i in 0..steps) {
                        val y = (i / steps.toFloat()) * height
                        val dx =
                            sin(y / wavePeriod * 2 * PI.toFloat() + currentWaveOffset) * waveAmplitude
                        lineTo(width + dx, y)
                    }

                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }

                drawPath(
                    path = path,
                    brush = Brush.horizontalGradient(
                        listOf(
                            colorScheme.scrim.copy(alpha = 0.1f + 0.1f * waveIntensity),
                            colorScheme.onPrimaryContainer.copy(alpha = 0.1f + 0.1f * waveIntensity),
                            colorScheme.primary.copy(alpha = 0.1f + 0.1f * waveIntensity),
                        ),
                    ),
                )

                val edgePath = Path().apply {
                    val waveAmplitude = if (isWavy)
                        (spacing.extraSmall + spacing.semiMedium * waveIntensity).toPx()
                    else 0f
                    val wavePeriod = height * 0.8f

                    val firstY = 0f
                    val firstDx =
                        sin(firstY / wavePeriod * 2 * PI.toFloat() + currentWaveOffset) * waveAmplitude
                    moveTo(width + firstDx, firstY)

                    val steps = 30
                    for (i in 1..steps) {
                        val y = (i / steps.toFloat()) * height
                        val dx =
                            sin(y / wavePeriod * 2 * PI.toFloat() + currentWaveOffset) * waveAmplitude
                        lineTo(width + dx, y)
                    }
                }

                drawPath(
                    path = edgePath,
                    color = Color.White.copy(alpha = 0.1f + 0.2f * waveIntensity),
                    style = Stroke(width = (spacing.extraTiny + spacing.extraTiny * waveIntensity).toPx()),
                )
            }
        }
    }
}
