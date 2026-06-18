package com.pointlessgames.hexagone.auth.ui

import androidx.compose.animation.core.EaseOutExpo
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.ui.theme.ThemeId
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.scaled
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.theme_unlocked_label
import hexagone.shared.generated.resources.theme_unlocked_overlay_title
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun ThemeUnlockedOverlay(
    themeId: ThemeId,
    onFinished: () -> Unit,
) {
    val spacing = MaterialTheme.spacing
    val cornerRadius = MaterialTheme.cornerRadius

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseOutExpo),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow_scale",
    )

    LaunchedEffect(themeId) {
        delay(3000.milliseconds)
        onFinished()
    }

    val previewColors = getThemeColors(themeId)
    val primaryColor = previewColors.tile1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(spacing.extraLarge.scaled),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.large.scaled),
        ) {
            Text(
                text = stringResource(Res.string.theme_unlocked_overlay_title),
                color = primaryColor,
                fontWeight = FontWeight.Black,
                fontSize = 32.sp.scaled,
                style = TextStyle(
                    shadow = Shadow(
                        color = primaryColor.copy(alpha = 0.5f),
                        offset = Offset(0f, 0f),
                        blurRadius = 40f * glowScale,
                    ),
                ),
                letterSpacing = 4.sp.scaled,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    scaleX = glowScale
                    scaleY = glowScale
                },
            )

            // Reward Info Card
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(cornerRadius.large))
                    .background(previewColors.darkBlue)
                    .border(
                        2.dp.scaled,
                        primaryColor,
                        RoundedCornerShape(cornerRadius.large),
                    )
                    .padding(spacing.large.scaled),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacing.medium.scaled),
                ) {
                    // Preview pattern inside
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp.scaled),
                    ) {
                        Box(
                            Modifier.size(32.dp.scaled)
                                .background(previewColors.tile1, RoundedCornerShape(4.dp.scaled)),
                        )
                        Box(
                            Modifier.size(32.dp.scaled)
                                .background(previewColors.tile2, RoundedCornerShape(4.dp.scaled)),
                        )
                        Box(
                            Modifier.size(32.dp.scaled)
                                .background(previewColors.tile4, RoundedCornerShape(4.dp.scaled)),
                        )
                    }

                    Spacer(Modifier.height(spacing.small.scaled))

                    Text(
                        text = getThemeNameString(themeId),
                        color = primaryColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp.scaled,
                        letterSpacing = 1.sp.scaled,
                    )
                }
            }
        }
    }
}
