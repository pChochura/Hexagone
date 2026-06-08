package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutExpo
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.ui.components.Position
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.app_name
import hexagone.shared.generated.resources.daily_challenge
import hexagone.shared.generated.resources.ic_daily_challenge
import hexagone.shared.generated.resources.best_score_label
import hexagone.shared.generated.resources.ic_achievements
import hexagone.shared.generated.resources.ic_leaderboards
import hexagone.shared.generated.resources.ic_settings
import hexagone.shared.generated.resources.level_label
import hexagone.shared.generated.resources.max_label
import hexagone.shared.generated.resources.perk_active_label
import hexagone.shared.generated.resources.perk_advance_queue_name
import hexagone.shared.generated.resources.perk_chain_merge_name
import hexagone.shared.generated.resources.perk_duplicate_tile_name
import hexagone.shared.generated.resources.perk_freeze_tile_name
import hexagone.shared.generated.resources.perk_fusion_name
import hexagone.shared.generated.resources.perk_increment_tile_name
import hexagone.shared.generated.resources.perk_move_tile_name
import hexagone.shared.generated.resources.perk_path_merge_name
import hexagone.shared.generated.resources.perk_remove_tile_name
import hexagone.shared.generated.resources.perk_skip_spawn_name
import hexagone.shared.generated.resources.perk_swap_tiles_name
import hexagone.shared.generated.resources.perk_undo_name
import hexagone.shared.generated.resources.score_label
import hexagone.shared.generated.resources.tier_overdrive
import hexagone.shared.generated.resources.tier_surge
import hexagone.shared.generated.resources.tier_zenith
import hexagone.shared.generated.resources.tooltip_achievements
import hexagone.shared.generated.resources.tooltip_leaderboard
import hexagone.shared.generated.resources.tooltip_settings
import org.jetbrains.compose.resources.stringResource

@Composable
fun ScoreSection(
    modifier: Modifier = Modifier,
    scoreProvider: () -> Int,
    bestScoreProvider: () -> Int,
    comboProvider: () -> Int,
    levelProvider: () -> Int,
    progressProvider: () -> Float,
    highestValueProvider: () -> Int,
    activePerkProvider: () -> Perk?,
    isDailyChallengeCompletedProvider: () -> Boolean = { false },
    onLevelClick: () -> Unit = {},
    onLeaderboardClick: () -> Unit = {},
    onAchievementsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onDailyChallengeClick: () -> Unit = {},
) {
    val score = scoreProvider()
    val bestScore = bestScoreProvider()
    val combo = comboProvider()
    val level = levelProvider()
    val progress = progressProvider()
    val highestValue = highestValueProvider()
    val activePerk = activePerkProvider()
    val isDailyChallengeCompleted = isDailyChallengeCompletedProvider()

    val waveIntensity = remember { Animatable(0f) }
    var previousScore by remember { mutableStateOf(score) }
    LaunchedEffect(score) {
        val addedScore = score - previousScore
        if (addedScore > 0) {
            // Intensity scales from 0.3 to 1.0 based on the amount scored
            // 200 points is considered a "major" move at higher levels
            val threshold = 100f + 25f * level
            val intensity = (addedScore / threshold).coerceIn(0.3f, 1.0f)
            waveIntensity.snapTo(intensity)
            waveIntensity.animateTo(0f, tween(1000))
        }
        previousScore = score
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { clip = false } // Allow massive combo pop to breathe
            .padding(top = MaterialTheme.spacing.small),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val spacing = MaterialTheme.spacing
        // Top Header with Game Name and Icons
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
            ) {
                HexagonIconButton(
                    onClick = onLeaderboardClick,
                    icon = Res.drawable.ic_leaderboards,
                    tooltip = Res.string.tooltip_leaderboard,
                    tooltipPosition = Position.BELOW,
                    size = 44.dp,
                    backgroundColor = Color.White.copy(alpha = 0.05f),
                    borderColor = Color.White.copy(alpha = 0.1f),
                )

                HexagonIconButton(
                    onClick = onAchievementsClick,
                    icon = Res.drawable.ic_achievements,
                    tooltip = Res.string.tooltip_achievements,
                    tooltipPosition = Position.BELOW,
                    size = 44.dp,
                    backgroundColor = Color.White.copy(alpha = 0.05f),
                    borderColor = Color.White.copy(alpha = 0.1f),
                )
            }

            Spacer(Modifier.weight(1f))

            Text(
                text = stringResource(Res.string.app_name).uppercase(),
                color = MaterialTheme.colorScheme.outlineVariant,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                letterSpacing = 2.sp,
            )

            Spacer(Modifier.weight(1f))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
            ) {
                HexagonIconButton(
                    onClick = onDailyChallengeClick,
                    icon = Res.drawable.ic_daily_challenge,
                    tooltip = Res.string.daily_challenge,
                    tooltipPosition = Position.BELOW,
                    size = 44.dp,
                    backgroundColor = if (isDailyChallengeCompleted) 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) 
                    else Color.White.copy(alpha = 0.05f),
                    borderColor = if (isDailyChallengeCompleted)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    else Color.White.copy(alpha = 0.1f),
                )

                HexagonIconButton(
                    onClick = onSettingsClick,
                    icon = Res.drawable.ic_settings,
                    tooltip = Res.string.tooltip_settings,
                    tooltipPosition = Position.BELOW,
                    size = 44.dp,
                    backgroundColor = Color.White.copy(alpha = 0.05f),
                    borderColor = Color.White.copy(alpha = 0.1f),
                )
            }
        }

        Spacer(Modifier.height(spacing.large))

        // Unified Score Section with Progress and Max/Perk Integrated
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .graphicsLayer { clip = false }, // Allow children (combo) to pop outside
        ) {
            WavyProgressBar(
                progress = progress,
                waveIntensity = waveIntensity.value,
                modifier = Modifier.matchParentSize(),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.extraLarge, vertical = spacing.large),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(Res.string.score_label),
                        color = Color.White.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp,
                    )
                    Spacer(Modifier.width(spacing.small))
                    Text(
                        text = "•",
                        color = Color.White.copy(alpha = 0.2f),
                        fontSize = 14.sp,
                    )
                    Spacer(Modifier.width(spacing.small))
                    Text(
                        text = stringResource(Res.string.level_label, level),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null,
                        ) {
                            onLevelClick()
                        },
                    )
                }

                Box(contentAlignment = Alignment.Center, modifier = Modifier.wrapContentHeight()) {
                    Text(
                        text = score.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 48.sp,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(Modifier.height(spacing.tiny))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall),
                ) {
                    Text(
                        text = stringResource(Res.string.best_score_label),
                        color = Color.White.copy(alpha = 0.3f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                    Text(
                        text = bestScore.toString(),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                    )
                }
            }

            // Integrated Combo Section
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = spacing.extraLarge)
                    .width(spacing.giant),
                contentAlignment = Alignment.Center,
            ) {
                val comboMultiplier = combo + 1
                AnimatedContent(
                    targetState = comboMultiplier,
                    transitionSpec = {
                        val settleDuration = when {
                            targetState > 8 -> 5000
                            targetState > 4 -> 4000
                            else -> 3000
                        }
                        (fadeIn(animationSpec = tween(200)) +
                                scaleIn(
                                    initialScale = 3f,
                                    animationSpec = tween(
                                        durationMillis = settleDuration,
                                        easing = EaseOutExpo,
                                    ),
                                ))
                            .togetherWith(fadeOut(animationSpec = tween(200)))
                            .using(SizeTransform(clip = false))
                    },
                    label = "combo_pop",
                ) { targetCombo ->
                    if (targetCombo > 1) {
                        val tier = when {
                            targetCombo >= 31 -> Res.string.tier_zenith
                            targetCombo >= 21 -> Res.string.tier_overdrive
                            targetCombo >= 11 -> Res.string.tier_surge
                            else -> null
                        }

                        val colorFraction = ((targetCombo - 1) / 9f).coerceIn(0f, 1f)
                        val baseColor = lerp(
                            MaterialTheme.colorScheme.surfaceDim, // Yellow/Gold
                            MaterialTheme.colorScheme.errorContainer, // Intense Orange/Red
                            colorFraction,
                        )

                        val comboColor = when (tier) {
                            Res.string.tier_surge -> MaterialTheme.colorScheme.scrim
                            Res.string.tier_overdrive -> MaterialTheme.colorScheme.inverseSurface
                            Res.string.tier_zenith -> MaterialTheme.colorScheme.surfaceBright
                            else -> baseColor
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "x$targetCombo",
                                color = comboColor,
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                style = TextStyle(
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        offset = Offset(4f, 4f),
                                        blurRadius = 8f,
                                    ),
                                ),
                                modifier = Modifier.graphicsLayer {
                                    rotationZ = -5f + colorFraction * 10f
                                },
                            )
                            if (tier != null) {
                                Text(
                                    text = stringResource(tier),
                                    color = comboColor,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp,
                                    letterSpacing = 2.sp,
                                    modifier = Modifier.offset(y = -spacing.extraSmall),
                                )
                            }
                        }
                    }
                }
            }

            // Integrated Max Value / Perk Section
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = spacing.extraLarge)
                    .width(spacing.giant),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedContent(
                    targetState = activePerk,
                    transitionSpec = {
                        (fadeIn() + scaleIn(initialScale = 0.75f)
                                togetherWith fadeOut() + scaleOut(targetScale = 0.75f)) using SizeTransform(
                            clip = false,
                        )
                    },
                    contentAlignment = Alignment.Center,
                    label = "integrated_max_value",
                ) { perk ->
                    if (perk != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(Res.string.perk_active_label),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                            )
                            val actionRes = when (perk) {
                                Perk.UNDO -> Res.string.perk_undo_name
                                Perk.MOVE_TILE -> Res.string.perk_move_tile_name
                                Perk.REMOVE_TILE -> Res.string.perk_remove_tile_name
                                Perk.ADVANCE_QUEUE -> Res.string.perk_advance_queue_name
                                Perk.SWAP_TILES -> Res.string.perk_swap_tiles_name
                                Perk.FUSION -> Res.string.perk_fusion_name
                                Perk.CHAIN_MERGE -> Res.string.perk_chain_merge_name
                                Perk.DUPLICATE_TILE -> Res.string.perk_duplicate_tile_name
                                Perk.SKIP_SPAWN -> Res.string.perk_skip_spawn_name
                                Perk.INCREMENT_TILE -> Res.string.perk_increment_tile_name
                                Perk.PATH_MERGE -> Res.string.perk_path_merge_name
                                Perk.FREEZE_TILE -> Res.string.perk_freeze_tile_name
                            }
                            Text(
                                text = stringResource(actionRes),
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 12.sp,
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(Res.string.max_label),
                                color = Color.White.copy(alpha = 0.3f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                            )
                            Spacer(Modifier.height(spacing.extraSmall))
                            val colorScheme = MaterialTheme.colorScheme
                            Hexagon(
                                value = highestValue.toString(),
                                backgroundColor = HexagonGridDefaults.getColorForValue(
                                    highestValue,
                                    colorScheme,
                                ).copy(alpha = 0.2f),
                                isOutline = true,
                                modifier = Modifier.size(spacing.huge).aspectRatio(1 / 0.866f),
                            )
                        }
                    }
                }
            }
        }
    }
}
