package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.model.ChallengeGoal
import com.pointlessgames.hexagone.game.model.DailyChallengeProgress
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
import hexagone.shared.generated.resources.ic_diamond
import hexagone.shared.generated.resources.ic_legendary_perk
import hexagone.shared.generated.resources.ic_locked
import hexagone.shared.generated.resources.ic_rare_perk
import hexagone.shared.generated.resources.ic_roll
import hexagone.shared.generated.resources.ic_star
import hexagone.shared.generated.resources.pattern_great_wall
import hexagone.shared.generated.resources.pattern_ring_of_fire
import hexagone.shared.generated.resources.pattern_the_prism
import hexagone.shared.generated.resources.pattern_twin_peaks
import hexagone.shared.generated.resources.progress_fraction
import hexagone.shared.generated.resources.reward_perk_label
import hexagone.shared.generated.resources.reward_score_label
import hexagone.shared.generated.resources.streak_label
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyChallengeDialog(
    challengesProvider: () -> List<DailyChallengeProgress>,
    streakProvider: () -> Int,
    completedDatesProvider: () -> Set<Long>,
    isStreakCollectedTodayProvider: () -> Boolean,
    onDismiss: () -> Unit,
) {
    val challenges = challengesProvider()
    val streak = streakProvider()
    val completedDates = completedDatesProvider()
    val isStreakCollectedToday = isStreakCollectedTodayProvider()

    ModalBottomSheet(
        contentWindowInsets = { WindowInsets.statusBars },
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = Color.Transparent,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.2f)) },
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(Res.string.daily_challenge).uppercase(),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = MaterialTheme.typography.headlineMedium.fontSize.scaled,
                ),
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.extraLarge.scaled),
            )

            Spacer(Modifier.height(MaterialTheme.spacing.medium.scaled))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small.scaled),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(challenges) { progress ->
                    ChallengeCard(progress)
                }

                item {
                    Spacer(Modifier.height(MaterialTheme.spacing.large.scaled))

                    Text(
                        text = stringResource(Res.string.streak_label, streak).uppercase(),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = MaterialTheme.typography.labelLarge.fontSize.scaled,
                        ),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp.scaled,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(MaterialTheme.spacing.medium.scaled))

                    StreakRow(streak, isStreakCollectedToday, completedDates)
                }

                item {
                    Spacer(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(bottom = MaterialTheme.spacing.large.scaled),
                    )
                }
            }
        }
    }
}

@Composable
private fun StreakRow(streak: Int, isStreakCollectedToday: Boolean, completedDates: Set<Long>) {
    val today = remember<LocalDate> {
        Clock.System.todayIn(TimeZone.currentSystemDefault())
    }

    val nextReward =
        com.pointlessgames.hexagone.game.logic.StreakMilestones.getRewardForStreak(streak + 1)

    Row(
        modifier = Modifier.fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = MaterialTheme.spacing.extraLarge.scaled),
        horizontalArrangement = Arrangement.spacedBy(
            space = MaterialTheme.spacing.small.scaled,
            alignment = Alignment.CenterHorizontally,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (i in 0 until 5) {
            val isToday = i == 3
            val isUpcoming = i > 3

            val dateForBox = today.plus(i - 3, DateTimeUnit.DAY)
            val dateSeed =
                dateForBox.year * 10000L + (dateForBox.month.ordinal + 1) * 100L + dateForBox.day

            val isChecked = completedDates.contains(dateSeed)
            val showReward = nextReward != null && (
                    (isToday && !isStreakCollectedToday) ||
                            (i == 4 && isStreakCollectedToday)
                    )

            StreakBox(
                isChecked = isChecked,
                isToday = isToday,
                isUpcoming = isUpcoming,
                date = dateForBox,
                reward = if (showReward) nextReward else null,
                modifier = Modifier.width(64.dp.scaled),
            )
        }
    }
}

@Composable
private fun StreakBox(
    isChecked: Boolean,
    isToday: Boolean,
    isUpcoming: Boolean,
    date: LocalDate,
    reward: com.pointlessgames.hexagone.game.logic.StreakReward?,
    modifier: Modifier = Modifier,
) {
    val dayName = date.dayOfWeek.name.take(3)
    val dayOfMonthString = date.day.toString()

    val backgroundColor = when {
        isChecked -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        isToday -> Color.White.copy(alpha = 0.1f)
        else -> Color.White.copy(alpha = 0.05f)
    }

    val borderColor = when {
        isToday -> MaterialTheme.colorScheme.primary
        isChecked -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        else -> Color.White.copy(alpha = 0.1f)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp.scaled),
    ) {
        Text(
            text = dayName,
            fontSize = 10.sp.scaled,
            fontWeight = FontWeight.Bold,
            color = if (isToday) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.4f),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(MaterialTheme.cornerRadius.small))
                .background(backgroundColor)
                .border(
                    2.dp.scaled,
                    borderColor,
                    RoundedCornerShape(MaterialTheme.cornerRadius.small),
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isChecked) {
                Icon(
                    painter = painterResource(Res.drawable.ic_star),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp.scaled),
                )
            } else if (isUpcoming) {
                Icon(
                    painter = painterResource(Res.drawable.ic_locked),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(16.dp.scaled),
                )
            } else {
                Text(
                    text = dayOfMonthString,
                    color = Color.White.copy(alpha = 0.3f),
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp.scaled,
                )
            }

            if (reward != null) {
                RewardTag(reward, Modifier.align(Alignment.BottomEnd).padding(4.dp.scaled))
            }
        }
    }
}

@Composable
private fun RewardTag(
    reward: com.pointlessgames.hexagone.game.logic.StreakReward,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp.scaled))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 4.dp.scaled, vertical = 2.dp.scaled),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp.scaled),
        ) {
            if (reward.diamonds > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp.scaled),
                ) {
                    Text(
                        text = reward.diamonds.toString(),
                        fontSize = 8.sp.scaled,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Icon(
                        painter = painterResource(Res.drawable.ic_diamond),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(10.dp.scaled),
                    )
                }
            }

            reward.perkRewards.forEach { (category, count) ->
                val icon = when (category) {
                    com.pointlessgames.hexagone.game.logic.PerkCategory.COMMON -> Res.drawable.ic_roll
                    com.pointlessgames.hexagone.game.logic.PerkCategory.RARE -> Res.drawable.ic_rare_perk
                    com.pointlessgames.hexagone.game.logic.PerkCategory.LEGENDARY -> Res.drawable.ic_legendary_perk
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp.scaled),
                ) {
                    if (count > 1) {
                        Text(
                            text = count.toString(),
                            fontSize = 8.sp.scaled,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(10.dp.scaled),
                    )
                }
            }
        }
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
    val shape = RoundedCornerShape(MaterialTheme.cornerRadius.medium)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.extraLarge.scaled)
            .height(IntrinsicSize.Min)
            .clip(shape)
            .border(
                1.dp,
                if (isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.White.copy(
                    alpha = 0.1f,
                ),
                shape,
            ),
    ) {
        WavyProgressBar(
            progress = progressFraction,
            modifier = Modifier.matchParentSize(),
            showContainer = true,
            containerColor = if (isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else Color.White.copy(
                alpha = 0.02f,
            ),
            borderColor = Color.Transparent,
            shape = shape,
            isWavy = !isCompleted,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium.scaled),
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

@Composable
private fun InfoCard(
    label: String,
    icon: org.jetbrains.compose.resources.DrawableResource,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(MaterialTheme.cornerRadius.small))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(MaterialTheme.spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(MaterialTheme.spacing.small))
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
        )
    }
}
