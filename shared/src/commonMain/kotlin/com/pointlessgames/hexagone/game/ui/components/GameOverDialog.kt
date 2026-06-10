package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.model.ChallengeGoal
import com.pointlessgames.hexagone.game.model.DailyChallengeProgress
import com.pointlessgames.hexagone.game.model.RankingInfo
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.spacing
import com.pointlessgames.hexagone.ui.theme.scaled
import hexagone.shared.generated.resources.*
import hexagone.shared.generated.resources.daily_challenge_goal_score
import hexagone.shared.generated.resources.daily_challenge_goal_tactical
import hexagone.shared.generated.resources.daily_challenge_goal_value
import hexagone.shared.generated.resources.game_over_title
import hexagone.shared.generated.resources.ic_back
import hexagone.shared.generated.resources.ic_star
import hexagone.shared.generated.resources.label_level
import hexagone.shared.generated.resources.label_max_combo
import hexagone.shared.generated.resources.label_max_piece
import hexagone.shared.generated.resources.new_best_label
import hexagone.shared.generated.resources.previous_best_label
import hexagone.shared.generated.resources.tooltip_view_board
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GameOverDialog(
    modifier: Modifier = Modifier,
    score: Int,
    bestScore: Int,
    level: Int,
    maxCombo: Int,
    @Suppress("UNUSED_PARAMETER") totalMerges: Int,
    highestValue: Int,
    rankingInfo: RankingInfo?,
    dailyChallenges: List<DailyChallengeProgress> = emptyList(),
    onViewBoard: () -> Unit,
) {
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(1500),
        label = "score_count_up",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "gameover_animations")

    val scoreGlowAlphaState = infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "score_glow",
    )

    val maxPieceGlowAlphaState = infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "max_piece_glow",
    )

    val isNewBest = score >= bestScore && score > 0

    val badgeRotationState = infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "badge_rotation",
    )

    val scoreGlowAlphaProvider = remember { { scoreGlowAlphaState.value } }
    val maxPieceGlowAlphaProvider = remember { { maxPieceGlowAlphaState.value } }
    val badgeRotationProvider = remember { { badgeRotationState.value } }

    val spacing = MaterialTheme.spacing
    val completedChallenges = dailyChallenges.filter { it.isCompleted }

    Box(
        modifier = modifier
            .fillMaxWidth(0.92f)
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                RoundedCornerShape(48.dp.scaled),
            )
            .border(
                2.dp.scaled,
                Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(48.dp.scaled),
            )
            .padding(spacing.extraLarge.scaled),
    ) {
        HexagonIconButton(
            onClick = onViewBoard,
            icon = Res.drawable.ic_back,
            tooltip = Res.string.tooltip_view_board,
            size = 48.dp.scaled,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .graphicsLayer { rotationZ = 180f }
                .padding(spacing.small.scaled),
            backgroundColor = Color.White.copy(alpha = 0.05f),
            borderColor = Color.White.copy(alpha = 0.1f),
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(Res.string.game_over_title).uppercase(),
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Black,
                fontSize = 24.sp.scaled,
                letterSpacing = 6.sp.scaled,
            )

            Spacer(Modifier.height(spacing.medium.scaled))

            AnimatedContent(rankingInfo) { rankingInfo ->
                rankingInfo?.let { rank ->
                    Box(
                        modifier = Modifier
                            .background(
                                Color.White.copy(alpha = 0.08f),
                                RoundedCornerShape(MaterialTheme.cornerRadius.full),
                            )
                            .padding(horizontal = spacing.large.scaled, vertical = spacing.extraSmall.scaled),
                    ) {
                        Text(
                            text = if (rank.isRegional) stringResource(Res.string.rank_regional, rank.rank) else stringResource(Res.string.rank_global, rank.rank),
                            color = Color(0xFFFFD54F), // Yellow from screenshot
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp.scaled,
                            letterSpacing = 1.sp.scaled,
                        )
                    }
                    Spacer(Modifier.height(spacing.medium.scaled))
                }
            }

            Spacer(Modifier.height(spacing.small.scaled))

            Box(contentAlignment = Alignment.Center) {
                // Score Glow - Deferred read to Draw phase via graphicsLayer
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer {
                            val alpha = scoreGlowAlphaProvider()
                            this.alpha = alpha * 0.5f
                            val scale = 1.2f + alpha * 0.2f
                            scaleX = scale
                            scaleY = scale
                        }
                        .background(
                            androidx.compose.ui.graphics.Brush.radialGradient(
                                colors = listOf(Color(0xFFFFD54F).copy(alpha = 0.4f), Color.Transparent),
                            )
                        )
                )

                Text(
                    text = animatedScore.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 84.sp.scaled,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = spacing.extraLarge.scaled),
                )

                if (isNewBest) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 16.dp.scaled, y = (-8).dp.scaled)
                            .graphicsLayer {
                                rotationZ = badgeRotationProvider()
                            }
                            .background(
                                Color(0xFFF06292), // Pink from screenshot
                                RoundedCornerShape(MaterialTheme.cornerRadius.full),
                            )
                            .padding(horizontal = spacing.medium.scaled, vertical = spacing.extraSmall.scaled),
                    ) {
                        Text(
                            text = stringResource(Res.string.new_best_label).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp.scaled,
                            letterSpacing = 1.sp.scaled,
                        )
                    }
                }
            }

            val bestScoreLabel = if (score > bestScore && score > 0) {
                stringResource(Res.string.previous_best_label, bestScore)
            } else {
                stringResource(Res.string.best_score_formatted, bestScore)
            }

            Text(
                text = bestScoreLabel,
                color = Color.White.copy(alpha = 0.4f),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp.scaled,
                letterSpacing = 1.sp.scaled,
            )

            if (completedChallenges.isNotEmpty()) {
                Spacer(Modifier.height(spacing.large.scaled))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(MaterialTheme.cornerRadius.medium))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(spacing.medium.scaled),
                    verticalArrangement = Arrangement.spacedBy(spacing.small.scaled),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(Res.string.daily_challenge).uppercase(),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp.scaled,
                        letterSpacing = 1.sp.scaled,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )

                    completedChallenges.forEach { progress ->
                        DailyChallengeSummaryRow(progress)
                    }
                }
            }

            Spacer(Modifier.height(spacing.large.scaled))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                // Level Stat
                GameOverStatHexagon(
                    label = stringResource(Res.string.label_level),
                    value = level.toString(),
                    backgroundColor = Color(0xFF37474F), // Dark teal/grey
                    size = 72.dp.scaled,
                )

                // Max Piece Stat (Larger and glowing)
                GameOverStatHexagon(
                    label = stringResource(Res.string.label_max_piece),
                    value = highestValue.toString(),
                    backgroundColor = Color(0xFF9345C4), // Purple
                    size = 92.dp.scaled,
                    labelColor = Color.White,
                    glowAlphaProvider = maxPieceGlowAlphaProvider,
                    glowColor = Color(0xFFBB86FC),
                )

                // Max Combo Stat
                GameOverStatHexagon(
                    label = stringResource(Res.string.label_max_combo),
                    value = maxCombo.toString(),
                    backgroundColor = Color(0xFF5D4037), // Dark brown
                    size = 72.dp.scaled,
                )
            }
        }
    }
}

@Composable
private fun DailyChallengeSummaryRow(
    progress: DailyChallengeProgress,
) {
    val challenge = progress.challenge
    val goalText = when (challenge.goal) {
        ChallengeGoal.MERGE_COUNT -> stringResource(Res.string.daily_challenge_goal_merge, challenge.target)
        ChallengeGoal.LEVEL_REACHED -> stringResource(Res.string.daily_challenge_goal_level, challenge.target)
        ChallengeGoal.COMBO_REACHED -> stringResource(Res.string.daily_challenge_goal_combo, challenge.target)
        ChallengeGoal.SCORE_REACHED -> stringResource(Res.string.daily_challenge_goal_score, challenge.target)
        ChallengeGoal.TACTICAL_MERGES -> stringResource(Res.string.daily_challenge_goal_tactical, challenge.target)
        ChallengeGoal.PIECE_VALUE_REACHED -> stringResource(Res.string.daily_challenge_goal_value, challenge.target)
        ChallengeGoal.MOVES_WITHOUT_PERK -> stringResource(Res.string.daily_challenge_goal_no_perks, challenge.target)
        ChallengeGoal.PERK_RESTRICTED_LEVEL -> stringResource(Res.string.daily_challenge_goal_perk_restriction, challenge.target, challenge.restrictedPerk?.let { stringResource(it.displayNameRes) } ?: "")
        ChallengeGoal.LEGENDARY_GAMBLE -> stringResource(Res.string.daily_challenge_goal_legendary_gamble)
        ChallengeGoal.GEOMETRIC_PATTERN -> {
            val patternName = when (challenge.patternId) {
                "ring_of_fire" -> stringResource(Res.string.pattern_ring_of_fire)
                "great_wall" -> stringResource(Res.string.pattern_great_wall)
                "twin_peaks" -> stringResource(Res.string.pattern_twin_peaks)
                "the_prism" -> stringResource(Res.string.pattern_the_prism)
                else -> ""
            }
            stringResource(Res.string.daily_challenge_goal_pattern, patternName)
        }
        ChallengeGoal.ELITE_SACRIFICE -> stringResource(Res.string.daily_challenge_goal_elite_sacrifice)
        ChallengeGoal.COMBO_MAINTENANCE -> stringResource(Res.string.daily_challenge_goal_combo_maintenance, challenge.target)
        ChallengeGoal.GHOST_HORDE -> stringResource(Res.string.daily_challenge_goal_ghost_horde, challenge.target)
        ChallengeGoal.PATH_MERGE_COUNT -> stringResource(Res.string.daily_challenge_goal_path_merge, challenge.target)
        ChallengeGoal.DIVERSITY_STREAK -> stringResource(Res.string.daily_challenge_goal_diversity)
        ChallengeGoal.FRUGAL_SURVIVOR -> stringResource(Res.string.daily_challenge_goal_frugal, challenge.target)
        ChallengeGoal.FROZEN_RECOVERY -> stringResource(Res.string.daily_challenge_goal_frozen_recovery)
    }

    val rewardText = when {
        challenge.rewardScore > 0 -> stringResource(Res.string.score_popup, challenge.rewardScore)
        challenge.rewardPerk != null -> stringResource(challenge.rewardPerk.displayNameRes).uppercase()
        else -> stringResource(Res.string.done)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                painter = painterResource(Res.drawable.ic_star),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp.scaled),
            )
            Spacer(Modifier.width(MaterialTheme.spacing.small.scaled))
            Text(
                text = goalText,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp.scaled,
                maxLines = 1,
            )
        }

        Text(
            text = rewardText,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black,
            fontSize = 10.sp.scaled,
        )
    }
}

@Composable
private fun GameOverStatHexagon(
    label: String,
    value: String,
    backgroundColor: Color,
    size: Dp,
    labelColor: Color = Color.White.copy(alpha = 0.6f),
    glowAlphaProvider: () -> Float = { 0f },
    glowColor: Color = Color.Transparent,
) {
    val spacing = MaterialTheme.spacing
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.medium.scaled),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(width = size + 16.dp.scaled, height = (size + 16.dp.scaled) * 0.866f)
                    .graphicsLayer { 
                        alpha = glowAlphaProvider()
                    }
                    .background(glowColor.copy(alpha = 0.4f), FlatTopHexagonShape()),
            )
            Box(
                modifier = Modifier
                    .size(width = size, height = size * 0.866f)
                    .background(backgroundColor, FlatTopHexagonShape())
                    .border(2.dp.scaled, Color.White.copy(alpha = 0.1f), FlatTopHexagonShape()),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = value,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = (size.value * 0.35f).sp, // This is already relative to size
                    textAlign = TextAlign.Center,
                )
            }
        }
        Text(
            text = label,
            color = labelColor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp.scaled,
            letterSpacing = 0.5.sp.scaled,
        )
    }
}
