package com.pointlessgames.hexagone.game.ui.components

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
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
import com.pointlessgames.hexagone.game.logic.PerkCategory
import com.pointlessgames.hexagone.game.logic.StreakMilestones
import com.pointlessgames.hexagone.game.model.DailyChallenge
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.scaled
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.bonus_reward_msg
import hexagone.shared.generated.resources.bonus_reward_title
import hexagone.shared.generated.resources.daily_challenge_completed
import hexagone.shared.generated.resources.daily_reward_granted
import hexagone.shared.generated.resources.ic_daily_challenge
import hexagone.shared.generated.resources.ic_diamond
import hexagone.shared.generated.resources.ic_legendary_perk
import hexagone.shared.generated.resources.ic_rare_perk
import hexagone.shared.generated.resources.ic_roll
import hexagone.shared.generated.resources.match_bonus_granted
import hexagone.shared.generated.resources.perk_category_common
import hexagone.shared.generated.resources.perk_category_legendary
import hexagone.shared.generated.resources.perk_category_rare
import hexagone.shared.generated.resources.reward_score_added
import hexagone.shared.generated.resources.streak_milestone_diamonds
import hexagone.shared.generated.resources.streak_milestone_perks
import hexagone.shared.generated.resources.streak_milestone_reached
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun DailyChallengeRewardOverlay(
    challenge: DailyChallenge,
    isFirstTimeToday: Boolean = true,
    isDayCompleted: Boolean = false,
    newStreak: Int = 0,
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

    val streakReward = remember(newStreak) {
        if (isDayCompleted) StreakMilestones.getRewardForStreak(newStreak) else null
    }

    LaunchedEffect(challenge, isDayCompleted) {
        delay(if (isDayCompleted) 5000.milliseconds else 3000.milliseconds)
        onFinished()
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.extraLarge.scaled),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.large.scaled),
        ) {
            Text(
                text = (if (isFirstTimeToday) stringResource(Res.string.daily_challenge_completed)
                else stringResource(Res.string.bonus_reward_title)).uppercase(),
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
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                    .border(
                        spacing.extraTiny.scaled,
                        Color.White.copy(alpha = 0.1f),
                        RoundedCornerShape(cornerRadius.large),
                    )
                    .padding(spacing.large.scaled),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium.scaled),
                ) {
                    Box(
                        modifier = Modifier
                            .size(spacing.giant.scaled)
                            .background(primaryColor.copy(alpha = 0.1f), CircleShape)
                            .border(
                                spacing.extraTiny.scaled,
                                primaryColor.copy(alpha = 0.3f),
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        androidx.compose.material3.Icon(
                            painter = painterResource(Res.drawable.ic_daily_challenge),
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(spacing.extraLarge.scaled),
                        )
                    }

                    Column {
                        if (challenge.rewardScore > 0) {
                            Text(
                                text = stringResource(
                                    Res.string.reward_score_added,
                                    challenge.rewardScore,
                                ),
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp.scaled,
                                letterSpacing = 1.sp.scaled,
                            )
                        }
                        challenge.rewardPerk?.let {
                            Text(
                                text = stringResource(it.displayNameRes).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp.scaled,
                                letterSpacing = 1.sp.scaled,
                            )
                        }
                        Text(
                            text = if (isFirstTimeToday) stringResource(Res.string.daily_reward_granted)
                            else stringResource(Res.string.match_bonus_granted),
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp.scaled,
                            lineHeight = 16.sp.scaled,
                        )
                    }
                }
            }

            if (!isFirstTimeToday) {
                Text(
                    text = stringResource(Res.string.bonus_reward_msg),
                    color = primaryColor.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp.scaled,
                    letterSpacing = 2.sp.scaled,
                )
            }

            if (isDayCompleted && (streakReward != null)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacing.medium.scaled),
                ) {
                    Text(
                        text = stringResource(Res.string.streak_milestone_reached, newStreak),
                        color = Color(0xFFFFD54F),
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp.scaled,
                        letterSpacing = 1.sp.scaled,
                        textAlign = TextAlign.Center,
                    )

                    Column(
                        modifier = Modifier.wrapContentWidth(),
                        verticalArrangement = Arrangement.spacedBy(spacing.medium.scaled),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (streakReward.diamonds > 0) {
                            RewardItem(
                                icon = Res.drawable.ic_diamond,
                                label = stringResource(
                                    Res.string.streak_milestone_diamonds,
                                    streakReward.diamonds,
                                ),
                                color = Color(0xFFFFD54F),
                            )
                        }

                        streakReward.perkRewards.forEach { (category, count) ->
                            val icon = when (category) {
                                PerkCategory.COMMON -> Res.drawable.ic_roll
                                PerkCategory.RARE -> Res.drawable.ic_rare_perk
                                PerkCategory.LEGENDARY -> Res.drawable.ic_legendary_perk
                            }
                            val color = when (category) {
                                PerkCategory.COMMON -> Color.Gray
                                PerkCategory.RARE -> Color(0xFF4FC3F7)
                                PerkCategory.LEGENDARY -> Color(0xFFFFD54F)
                            }
                            val categoryName = when (category) {
                                PerkCategory.COMMON -> stringResource(Res.string.perk_category_common)
                                PerkCategory.RARE -> stringResource(Res.string.perk_category_rare)
                                PerkCategory.LEGENDARY -> stringResource(Res.string.perk_category_legendary)
                            }
                            RewardItem(
                                icon = icon,
                                label = stringResource(
                                    Res.string.streak_milestone_perks,
                                    count,
                                    categoryName,
                                ),
                                color = color,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RewardItem(
    icon: DrawableResource,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.spacing
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp.scaled))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp.scaled, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp.scaled))
            .padding(horizontal = spacing.medium.scaled, vertical = spacing.small.scaled),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.small.scaled),
    ) {
        androidx.compose.material3.Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp.scaled),
        )
        Text(
            text = label.uppercase(),
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Black,
            fontSize = 10.sp.scaled,
            letterSpacing = 0.5.sp.scaled,
        )
    }
}
