package com.pointlessgames.hexagone.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.LocalNavigator
import com.pointlessgames.hexagone.game.GameViewModel
import com.pointlessgames.hexagone.game.model.ChallengeGoal
import com.pointlessgames.hexagone.game.model.DailyChallengeProgress
import com.pointlessgames.hexagone.game.ui.components.ScreenScaffold
import com.pointlessgames.hexagone.game.ui.components.ShopSectionTitle
import com.pointlessgames.hexagone.game.ui.components.displayNameRes
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.scaled
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.daily_challenge
import hexagone.shared.generated.resources.daily_challenge_completed
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
import hexagone.shared.generated.resources.ic_daily_challenge
import hexagone.shared.generated.resources.ic_star
import hexagone.shared.generated.resources.max_label
import hexagone.shared.generated.resources.pattern_great_wall
import hexagone.shared.generated.resources.pattern_ring_of_fire
import hexagone.shared.generated.resources.pattern_the_prism
import hexagone.shared.generated.resources.pattern_twin_peaks
import hexagone.shared.generated.resources.progress_fraction
import hexagone.shared.generated.resources.reward_perk_label
import hexagone.shared.generated.resources.reward_score_label
import hexagone.shared.generated.resources.streak_explanation
import hexagone.shared.generated.resources.streak_label
import hexagone.shared.generated.resources.streak_milestones_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DailyMissionsScreen(
    viewModel: GameViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val navigator = LocalNavigator.current
    val spacing = MaterialTheme.spacing

    ScreenScaffold(
        title = stringResource(Res.string.daily_challenge),
        onBack = { navigator.pop() },
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { clip = false },
            verticalArrangement = Arrangement.spacedBy(spacing.small.scaled),
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding(),
                bottom = spacing.extraLarge.scaled,
            ),
        ) {
            items(uiState.dailyChallenges) { progress ->
                Box(modifier = Modifier.padding(horizontal = spacing.extraLarge.scaled)) {
                    ChallengeCard(progress)
                }
            }

            item {
                Spacer(Modifier.height(spacing.large.scaled))
            }

            item {
                Box(modifier = Modifier.padding(horizontal = spacing.extraLarge.scaled)) {
                    ShopSectionTitle(text = stringResource(Res.string.streak_milestones_title))
                }
            }

            item {
                Box(modifier = Modifier.padding(horizontal = spacing.extraLarge.scaled)) {
                    Text(
                        text = stringResource(Res.string.streak_explanation),
                        color = Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp.scaled,
                        lineHeight = 16.sp.scaled,
                        modifier = Modifier.padding(bottom = spacing.medium.scaled),
                    )
                }
            }

            item {
                MilestoneMap(
                    currentStreak = uiState.challengeStreak,
                    isCollectedToday = uiState.isStreakCollectedToday,
                    onMilestoneClick = viewModel::onShowMilestoneDetails,
                )
            }

            item {
                Spacer(Modifier.height(spacing.medium.scaled))
            }

            item {
                Box(modifier = Modifier.padding(horizontal = spacing.extraLarge.scaled)) {
                    Text(
                        text = stringResource(
                            Res.string.streak_label,
                            uiState.challengeStreak,
                        ).uppercase(),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp.scaled,
                        letterSpacing = 1.sp.scaled,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun MilestoneMap(
    currentStreak: Int,
    isCollectedToday: Boolean,
    onMilestoneClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val milestones = remember {
        listOf(3, 5, 7, 14, 21, 30, 60, 90, 120, 150, 180, 210, 240, 270, 300, 330, 365)
    }
    val extraLargePadding = MaterialTheme.spacing.extraLarge.scaled

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = MaterialTheme.spacing.small.scaled),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium.scaled),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.width(extraLargePadding - MaterialTheme.spacing.medium.scaled))

        milestones.forEach { day ->
            MilestoneNode(
                day = day,
                isReached = currentStreak >= day,
                isNext = !isCollectedToday && day > currentStreak && (milestones.firstOrNull { it > currentStreak } == day),
                onClick = { onMilestoneClick(day) },
            )
        }

        Spacer(Modifier.width(extraLargePadding))
    }
}

@Composable
private fun MilestoneNode(
    day: Int,
    isReached: Boolean,
    isNext: Boolean,
    onClick: () -> Unit,
) {
    val color = if (isReached) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.2f)
    val spacing = MaterialTheme.spacing

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(60.dp.scaled),
    ) {
        Box(
            modifier = Modifier
                .size(50.dp.scaled)
                .clip(CircleShape)
                .clickable { onClick() }
                .background(MaterialTheme.colorScheme.background, CircleShape)
                .border(
                    width = if (isNext) 2.dp.scaled else 1.dp.scaled,
                    color = if (isNext) MaterialTheme.colorScheme.primary else color,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = day.toString(),
                    color = if (isReached || isNext) Color.White else Color.White.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp.scaled,
                )
                Text(
                    text = stringResource(Res.string.max_label).uppercase(),
                    color = color.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.sp.scaled,
                )
            }
        }

        Spacer(Modifier.height(spacing.extraSmall.scaled))

        Text(
            text = "DAY $day",
            color = if (isReached) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.3f),
            fontSize = 9.sp.scaled,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ChallengeCard(
    progress: DailyChallengeProgress,
) {
    val challenge = progress.challenge
    val isCompleted = progress.isCompleted

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

    val progressFraction =
        (progress.progress.toFloat() / challenge.target.toFloat()).coerceIn(0f, 1f)
    val shape = RoundedCornerShape(MaterialTheme.cornerRadius.medium.scaled)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(shape)
            .background(MaterialTheme.colorScheme.background)
            .border(
                1.dp.scaled,
                if (isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.White.copy(
                    alpha = 0.1f,
                ),
                shape,
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progressFraction)
                .fillMaxHeight()
                .background(
                    if (isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else Color.White.copy(alpha = 0.03f),
                ),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.large.scaled),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp.scaled)
                    .clip(CircleShape)
                    .background(
                        if (isCompleted) Brush.linearGradient(
                            listOf(
                                Color(0xFFF2994A),
                                Color(0xFFF2C94C),
                            ),
                        )
                        else Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.1f),
                                Color.White.copy(alpha = 0.05f),
                            ),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(if (isCompleted) Res.drawable.ic_star else Res.drawable.ic_daily_challenge),
                    contentDescription = null,
                    tint = if (isCompleted) Color.White else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp.scaled),
                )
            }

            Spacer(Modifier.width(MaterialTheme.spacing.medium.scaled))

            Column {
                Text(
                    text = goalText,
                    color = if (isCompleted) Color.White else Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp.scaled,
                )

                val rewardText = when {
                    challenge.rewardScore > 0 -> stringResource(
                        Res.string.reward_score_label,
                        challenge.rewardScore,
                    )

                    challenge.rewardPerk != null -> stringResource(
                        Res.string.reward_perk_label,
                        stringResource(challenge.rewardPerk.displayNameRes),
                    )

                    else -> ""
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isCompleted) stringResource(Res.string.daily_challenge_completed)
                        else stringResource(
                            Res.string.progress_fraction,
                            progress.progress,
                            challenge.target,
                        ),
                        color = if (isCompleted) MaterialTheme.colorScheme.primary else Color.White.copy(
                            alpha = 0.4f,
                        ),
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp.scaled,
                    )

                    if (rewardText.isNotEmpty()) {
                        Text(
                            text = "  •  ",
                            color = Color.White.copy(alpha = 0.2f),
                            fontSize = 12.sp.scaled,
                        )
                        Text(
                            text = rewardText.uppercase(),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = if (isCompleted) 1f else 0.8f),
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp.scaled,
                            letterSpacing = 0.5.sp.scaled,
                        )
                    }
                }
            }
        }
    }
}
