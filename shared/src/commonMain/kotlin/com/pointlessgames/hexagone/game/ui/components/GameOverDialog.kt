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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.pointlessgames.hexagone.ui.theme.scaled
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.best_score_formatted
import hexagone.shared.generated.resources.daily_challenge
import hexagone.shared.generated.resources.daily_challenge_goal_combo
import hexagone.shared.generated.resources.daily_challenge_goal_combo_maintenance
import hexagone.shared.generated.resources.daily_challenge_goal_diversity
import hexagone.shared.generated.resources.daily_challenge_goal_elite_sacrifice
import hexagone.shared.generated.resources.daily_challenge_goal_frozen_recovery
import hexagone.shared.generated.resources.daily_challenge_goal_frugal
import hexagone.shared.generated.resources.daily_challenge_goal_ghost_horde
import hexagone.shared.generated.resources.daily_challenge_goal_legendary_gamble
import hexagone.shared.generated.resources.daily_challenge_goal_level
import hexagone.shared.generated.resources.daily_challenge_goal_merge
import hexagone.shared.generated.resources.daily_challenge_goal_no_perks
import hexagone.shared.generated.resources.daily_challenge_goal_path_merge
import hexagone.shared.generated.resources.daily_challenge_goal_pattern
import hexagone.shared.generated.resources.daily_challenge_goal_perk_restriction
import hexagone.shared.generated.resources.daily_challenge_goal_score
import hexagone.shared.generated.resources.daily_challenge_goal_tactical
import hexagone.shared.generated.resources.daily_challenge_goal_value
import hexagone.shared.generated.resources.done
import hexagone.shared.generated.resources.game_over_title
import hexagone.shared.generated.resources.ic_hide
import hexagone.shared.generated.resources.ic_leaderboards
import hexagone.shared.generated.resources.ic_play_again
import hexagone.shared.generated.resources.ic_share
import hexagone.shared.generated.resources.ic_star
import hexagone.shared.generated.resources.label_level
import hexagone.shared.generated.resources.label_max_combo
import hexagone.shared.generated.resources.label_max_piece
import hexagone.shared.generated.resources.leaderboard_disabled_debug
import hexagone.shared.generated.resources.leaderboard_title
import hexagone.shared.generated.resources.new_best_label
import hexagone.shared.generated.resources.pattern_great_wall
import hexagone.shared.generated.resources.pattern_ring_of_fire
import hexagone.shared.generated.resources.pattern_the_prism
import hexagone.shared.generated.resources.pattern_twin_peaks
import hexagone.shared.generated.resources.play_again_button
import hexagone.shared.generated.resources.previous_best_label
import hexagone.shared.generated.resources.rank_global
import hexagone.shared.generated.resources.rank_regional
import hexagone.shared.generated.resources.score_popup
import hexagone.shared.generated.resources.share_label
import hexagone.shared.generated.resources.tooltip_leaderboard
import hexagone.shared.generated.resources.tooltip_share
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
    persistentCompletedMissionIds: Set<String> = emptySet(),
    debugUsed: Boolean = false,
    onViewBoard: () -> Unit,
    onRestart: () -> Unit,
    onShare: () -> Unit,
    onLeaderboard: () -> Unit,
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

    BoxWithConstraints(
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
        val isLandscape = maxWidth > maxHeight

        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
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

            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.extraLarge.scaled)
                ) {
                    // Left Column: Score
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ScoreSectionContent(
                            rankingInfo = rankingInfo,
                            animatedScore = animatedScore,
                            isNewBest = isNewBest,
                            scoreGlowAlphaProvider = scoreGlowAlphaProvider,
                            badgeRotationProvider = badgeRotationProvider,
                            bestScore = bestScore,
                            score = score,
                            debugUsed = debugUsed,
                            spacing = spacing
                        )
                    }

                    // Right Column: Stats & Challenges
                    Column(
                        modifier = Modifier.weight(1.2f).height(IntrinsicSize.Max),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(spacing.medium.scaled)
                        ) {
                            StatsRow(
                                level = level,
                                highestValue = highestValue,
                                maxCombo = maxCombo,
                                maxPieceGlowAlphaProvider = maxPieceGlowAlphaProvider
                            )

                            if (completedChallenges.isNotEmpty()) {
                                ChallengesSummary(
                                    completedChallenges = completedChallenges,
                                    persistentCompletedMissionIds = persistentCompletedMissionIds,
                                    spacing = spacing
                                )
                            }
                        }

                        Spacer(Modifier.height(spacing.medium.scaled))

                        DialogActions(
                            onRestart = onRestart,
                            onShare = onShare,
                            onLeaderboard = onLeaderboard
                        )
                    }
                }
            } else {
                // Portrait Layout
                ScoreSectionContent(
                    rankingInfo = rankingInfo,
                    animatedScore = animatedScore,
                    isNewBest = isNewBest,
                    scoreGlowAlphaProvider = scoreGlowAlphaProvider,
                    badgeRotationProvider = badgeRotationProvider,
                    bestScore = bestScore,
                    score = score,
                    debugUsed = debugUsed,
                    spacing = spacing
                )

                if (completedChallenges.isNotEmpty()) {
                    Spacer(Modifier.height(spacing.large.scaled))
                    ChallengesSummary(
                        completedChallenges = completedChallenges,
                        persistentCompletedMissionIds = persistentCompletedMissionIds,
                        spacing = spacing
                    )
                }

                Spacer(Modifier.height(spacing.large.scaled))

                StatsRow(
                    level = level,
                    highestValue = highestValue,
                    maxCombo = maxCombo,
                    maxPieceGlowAlphaProvider = maxPieceGlowAlphaProvider
                )
            }
        }

        HexagonIconButton(
            onClick = onViewBoard,
            icon = Res.drawable.ic_hide,
            tooltip = Res.string.tooltip_view_board,
            size = 36.dp.scaled,
            modifier = Modifier.align(Alignment.TopEnd),
            backgroundColor = Color.White.copy(alpha = 0.05f),
            borderColor = Color.White.copy(alpha = 0.1f),
        )
    }
}

@Composable
private fun DialogActions(
    onRestart: () -> Unit,
    onShare: () -> Unit,
    onLeaderboard: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HexagonIconButton(
            onClick = onShare,
            icon = Res.drawable.ic_share,
            label = stringResource(Res.string.share_label),
            tooltip = Res.string.tooltip_share,
            size = 50.dp.scaled
        )

        HexagonIconButton(
            onClick = onRestart,
            icon = Res.drawable.ic_play_again,
            label = stringResource(Res.string.play_again_button),
            size = 64.dp.scaled,
            backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        HexagonIconButton(
            onClick = onLeaderboard,
            icon = Res.drawable.ic_leaderboards,
            label = stringResource(Res.string.leaderboard_title),
            tooltip = Res.string.tooltip_leaderboard,
            size = 50.dp.scaled
        )
    }
}

@Composable
private fun ScoreSectionContent(
    rankingInfo: RankingInfo?,
    animatedScore: Int,
    isNewBest: Boolean,
    scoreGlowAlphaProvider: () -> Float,
    badgeRotationProvider: () -> Float,
    bestScore: Int,
    score: Int,
    debugUsed: Boolean,
    spacing: com.pointlessgames.hexagone.ui.theme.Spacing
) {
    AnimatedContent(rankingInfo) { rankingInfo ->
        rankingInfo?.let { rank ->
            Box(
                modifier = Modifier
                    .background(
                        Color.White.copy(alpha = 0.08f),
                        RoundedCornerShape(MaterialTheme.cornerRadius.full),
                    )
                    .padding(
                        horizontal = spacing.large.scaled,
                        vertical = spacing.extraSmall.scaled,
                    ),
            ) {
                Text(
                    text = if (rank.isRegional) stringResource(
                        Res.string.rank_regional,
                        rank.rank,
                    ) else stringResource(Res.string.rank_global, rank.rank),
                    color = Color(0xFFFFD54F),
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
                        colors = listOf(
                            Color(0xFFFFD54F).copy(alpha = 0.4f),
                            Color.Transparent,
                        ),
                    ),
                ),
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
                        Color(0xFFF06292),
                        RoundedCornerShape(MaterialTheme.cornerRadius.full),
                    )
                    .padding(
                        horizontal = spacing.medium.scaled,
                        vertical = spacing.extraSmall.scaled,
                    ),
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

    if (debugUsed) {
        Spacer(Modifier.height(spacing.extraSmall.scaled))
        Text(
            text = stringResource(Res.string.leaderboard_disabled_debug).uppercase(),
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Black,
            fontSize = 10.sp.scaled,
            letterSpacing = 1.sp.scaled,
        )
    }
}

@Composable
private fun ChallengesSummary(
    completedChallenges: List<DailyChallengeProgress>,
    persistentCompletedMissionIds: Set<String>,
    spacing: com.pointlessgames.hexagone.ui.theme.Spacing
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(MaterialTheme.cornerRadius.medium))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(
                horizontal = spacing.medium.scaled,
                vertical = spacing.small.scaled,
            ),
        verticalArrangement = Arrangement.spacedBy(spacing.extraSmall.scaled),
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
            DailyChallengeSummaryRow(
                progress = progress,
                isPersistentCompleted = persistentCompletedMissionIds.contains(progress.challenge.id)
            )
        }
    }
}

@Composable
private fun StatsRow(
    level: Int,
    highestValue: Int,
    maxCombo: Int,
    maxPieceGlowAlphaProvider: () -> Float
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        GameOverStatHexagon(
            modifier = Modifier.fillMaxHeight(),
            label = stringResource(Res.string.label_level),
            value = level.toString(),
            backgroundColor = Color(0xFF37474F),
            size = 48.dp.scaled,
        )

        GameOverStatHexagon(
            modifier = Modifier.fillMaxHeight(),
            label = stringResource(Res.string.label_max_piece),
            value = highestValue.toString(),
            backgroundColor = Color(0xFF9345C4),
            size = 64.dp.scaled,
            labelColor = Color.White,
            glowAlphaProvider = maxPieceGlowAlphaProvider,
            glowColor = Color(0xFFBB86FC),
        )

        GameOverStatHexagon(
            modifier = Modifier.fillMaxHeight(),
            label = stringResource(Res.string.label_max_combo),
            value = maxCombo.toString(),
            backgroundColor = Color(0xFF5D4037),
            size = 48.dp.scaled,
        )
    }
}

@Composable
private fun DailyChallengeSummaryRow(
    progress: DailyChallengeProgress,
    isPersistentCompleted: Boolean,
) {
    val challenge = progress.challenge
    val goalText = when (challenge.goal) {
        ChallengeGoal.MERGE_COUNT -> stringResource(
            Res.string.daily_challenge_goal_merge,
            challenge.target,
        )

        ChallengeGoal.LEVEL_REACHED -> stringResource(
            Res.string.daily_challenge_goal_level,
            challenge.target,
        )

        ChallengeGoal.COMBO_REACHED -> stringResource(
            Res.string.daily_challenge_goal_combo,
            challenge.target,
        )

        ChallengeGoal.SCORE_REACHED -> stringResource(
            Res.string.daily_challenge_goal_score,
            challenge.target,
        )

        ChallengeGoal.TACTICAL_MERGES -> stringResource(
            Res.string.daily_challenge_goal_tactical,
            challenge.target,
        )

        ChallengeGoal.PIECE_VALUE_REACHED -> stringResource(
            Res.string.daily_challenge_goal_value,
            challenge.target,
        )

        ChallengeGoal.MOVES_WITHOUT_PERK -> stringResource(
            Res.string.daily_challenge_goal_no_perks,
            challenge.target,
        )

        ChallengeGoal.PERK_RESTRICTED_LEVEL -> stringResource(
            Res.string.daily_challenge_goal_perk_restriction,
            challenge.target,
            challenge.restrictedPerk?.let { stringResource(it.displayNameRes) } ?: "",
        )

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
        ChallengeGoal.COMBO_MAINTENANCE -> stringResource(
            Res.string.daily_challenge_goal_combo_maintenance,
            challenge.target,
        )

        ChallengeGoal.GHOST_HORDE -> stringResource(
            Res.string.daily_challenge_goal_ghost_horde,
            challenge.target,
        )

        ChallengeGoal.PATH_MERGE_COUNT -> stringResource(
            Res.string.daily_challenge_goal_path_merge,
            challenge.target,
        )

        ChallengeGoal.DIVERSITY_STREAK -> stringResource(Res.string.daily_challenge_goal_diversity)
        ChallengeGoal.FRUGAL_SURVIVOR -> stringResource(
            Res.string.daily_challenge_goal_frugal,
            challenge.target,
        )

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
                tint = if (isPersistentCompleted) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(14.dp.scaled),
            )
            Spacer(Modifier.width(MaterialTheme.spacing.extraSmall.scaled))
            Text(
                text = goalText,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp.scaled,
                maxLines = 1,
            )
            
            if (isPersistentCompleted) {
                Spacer(Modifier.width(MaterialTheme.spacing.extraSmall.scaled))
                Text(
                    text = "STREAK+",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    fontSize = 8.sp.scaled,
                )
            }
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
    modifier: Modifier = Modifier,
    labelColor: Color = Color.White.copy(alpha = 0.6f),
    glowAlphaProvider: () -> Float = { 0f },
    glowColor: Color = Color.Transparent,
) {
    val spacing = MaterialTheme.spacing
    Column(
        modifier = modifier.width(IntrinsicSize.Min),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.small.scaled),
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
            modifier = Modifier.fillMaxWidth(),
            text = label,
            color = labelColor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp.scaled,
            lineHeight = 14.sp.scaled,
            letterSpacing = 0.5.sp.scaled,
            textAlign = TextAlign.Center,
        )
    }
}
