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
import com.pointlessgames.hexagone.game.model.DailyChallenge
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.*
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun DailyChallengeRewardOverlay(
    challenge: DailyChallenge,
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

    LaunchedEffect(challenge) {
        delay(3000.milliseconds)
        onFinished()
    }

    val primaryColor = MaterialTheme.colorScheme.primary

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
                text = stringResource(Res.string.daily_challenge_completed).uppercase(),
                color = primaryColor,
                fontWeight = FontWeight.Black,
                fontSize = 32.sp,
                style = TextStyle(
                    shadow = Shadow(
                        color = primaryColor.copy(alpha = 0.5f),
                        offset = Offset(0f, 0f),
                        blurRadius = 40f * glowScale
                    )
                ),
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    scaleX = glowScale
                    scaleY = glowScale
                }
            )

            Spacer(Modifier.height(spacing.extraLarge))

            // Reward Info Card
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
                            .background(primaryColor.copy(alpha = 0.1f), CircleShape)
                            .border(spacing.extraTiny, primaryColor.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            painter = org.jetbrains.compose.resources.painterResource(Res.drawable.ic_daily_challenge),
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(spacing.extraLarge)
                        )
                    }

                    Column {
                        if (challenge.rewardScore > 0) {
                            Text(
                                text = stringResource(Res.string.reward_score_added, challenge.rewardScore),
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = 1.sp
                            )
                        }
                        if (challenge.rewardPerk != null) {
                            Text(
                                text = stringResource(challenge.rewardPerk.displayNameRes).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = stringResource(Res.string.daily_reward_granted),
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
