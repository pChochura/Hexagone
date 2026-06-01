package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.pointlessgames.hexagone.game.model.ComboTier
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.tier_overdrive
import hexagone.shared.generated.resources.tier_surge
import hexagone.shared.generated.resources.tier_zenith
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun TierRewardOverlay(
    tier: ComboTier,
    perk: Perk,
    onFinished: () -> Unit
) {
    val spacing = MaterialTheme.spacing
    val cornerRadius = MaterialTheme.cornerRadius
    
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseOutExpo),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    LaunchedEffect(tier) {
        // Overlay stays for a fixed duration, the entrance/exit is handled by parent's AnimatedVisibility
        delay(3000.milliseconds)
        onFinished()
    }

    val tierColor = when (tier) {
        ComboTier.SURGE -> MaterialTheme.colorScheme.scrim
        ComboTier.OVERDRIVE -> MaterialTheme.colorScheme.inverseSurface
        ComboTier.ZENITH -> MaterialTheme.colorScheme.surfaceBright
    }

    val tierName = when (tier) {
        ComboTier.SURGE -> Res.string.tier_surge
        ComboTier.OVERDRIVE -> Res.string.tier_overdrive
        ComboTier.ZENITH -> Res.string.tier_zenith
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.extraLarge),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(tierName).uppercase(),
                color = tierColor,
                fontWeight = FontWeight.Black,
                fontSize = 48.sp,
                style = TextStyle(
                    shadow = Shadow(
                        color = tierColor.copy(alpha = 0.5f),
                        offset = Offset(0f, 0f),
                        blurRadius = 40f * glowScale
                    )
                ),
                letterSpacing = 8.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    scaleX = glowScale
                    scaleY = glowScale
                }
            )

            Spacer(Modifier.height(spacing.medium))

            Text(
                text = "PERK UNLOCKED",
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(spacing.extraLarge))

            // Perk Info Card
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(cornerRadius.large))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                    .border(spacing.extraTiny, Color.White.copy(alpha = 0.1f), RoundedCornerShape(cornerRadius.large))
                    .padding(spacing.large)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium)
                ) {
                    Box(
                        modifier = Modifier
                            .size(spacing.giant)
                            .background(tierColor.copy(alpha = 0.1f), CircleShape)
                            .border(spacing.extraTiny, tierColor.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        PerkIcon(
                            perk = perk,
                            modifier = Modifier.size(spacing.extraLarge),
                            color = tierColor
                        )
                    }

                    Column {
                        Text(
                            text = stringResource(perk.displayNameRes).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = stringResource(perk.descriptionRes),
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
            
            if (tier == ComboTier.ZENITH) {
                Spacer(Modifier.height(spacing.large))
                Text(
                    text = "+1 TO ALL TILES ON BOARD!",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    style = TextStyle(
                        shadow = Shadow(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            offset = Offset(0f, 0f),
                            blurRadius = 10f
                        )
                    )
                )
            }
        }
    }
}
