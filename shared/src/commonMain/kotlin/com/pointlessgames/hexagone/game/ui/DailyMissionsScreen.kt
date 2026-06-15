package com.pointlessgames.hexagone.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.pointlessgames.hexagone.game.logic.PerkCategory
import com.pointlessgames.hexagone.game.logic.StreakMilestones
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
import hexagone.shared.generated.resources.ic_diamond
import hexagone.shared.generated.resources.ic_legendary_perk
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
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

@Composable
internal fun DailyMissionsScreen(
    viewModel: GameViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val navigator = LocalNavigator.current
    val spacing = MaterialTheme.spacing

    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }

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
            // Prominent Streak Section (Top)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = spacing.extraLarge.scaled,
                            vertical = spacing.large.scaled,
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacing.small.scaled),
                ) {
                    Text(
                        text = stringResource(
                            Res.string.streak_label,
                            uiState.challengeStreak,
                        ).uppercase(),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 32.sp.scaled,
                            letterSpacing = 2.sp.scaled,
                        ),
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                    )

                    Text(
                        text = "Don't let the fire go out!".uppercase(),
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp.scaled,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp.scaled,
                    )
                }
            }

            // Next Milestone Card (Top)
            item {
                val nextMilestone = remember(uiState.challengeStreak) {
                    val milestones = listOf(
                        3,
                        5,
                        7,
                        14,
                        21,
                        30,
                        60,
                        90,
                        120,
                        150,
                        180,
                        210,
                        240,
                        270,
                        300,
                        330,
                        365,
                    )
                    milestones.firstOrNull { it > uiState.challengeStreak }
                }

                if (nextMilestone != null) {
                    Box(modifier = Modifier.padding(horizontal = spacing.extraLarge.scaled)) {
                        NextRewardCard(
                            streak = uiState.challengeStreak,
                            nextMilestone = nextMilestone,
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(spacing.medium.scaled))
            }

            // Active Missions
            items(uiState.dailyChallenges) { progress ->
                Box(modifier = Modifier.padding(horizontal = spacing.extraLarge.scaled)) {
                    ChallengeCard(progress)
                }
            }

            item {
                Spacer(Modifier.height(spacing.large.scaled))
            }

            // Calendar Section (Bottom)
            item {
                Box(modifier = Modifier.padding(horizontal = spacing.extraLarge.scaled)) {
                    ShopSectionTitle(text = "MISSION LOG")
                }
            }

            item {
                Box(modifier = Modifier.padding(horizontal = spacing.extraLarge.scaled)) {
                    HexCalendar(
                        today = today,
                        completedDates = uiState.completedChallengeDates,
                    )
                }
            }
        }
    }
}

@Composable
private fun HexCalendar(
    today: LocalDate,
    completedDates: Set<Long>,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.spacing
    val firstDayOfMonth = LocalDate(today.year, today.month, 1)
    val lastDayOfMonth = firstDayOfMonth.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
    val daysInMonth = lastDayOfMonth.day

    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.ordinal // 0 = Mon, 6 = Sun
    val totalGridCells = ((daysInMonth + firstDayOfWeek + 6) / 7) * 7

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(MaterialTheme.cornerRadius.medium.scaled))
            .background(MaterialTheme.colorScheme.background)
            .border(
                1.dp.scaled,
                Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(MaterialTheme.cornerRadius.medium.scaled),
            )
            .padding(spacing.large.scaled),
        verticalArrangement = Arrangement.spacedBy(spacing.medium.scaled),
    ) {
        // Month Header
        Text(
            text = "${today.month.name} ${today.year}".uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp.scaled,
            letterSpacing = 2.sp.scaled,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        // Day Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                Text(
                    text = day,
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 10.sp.scaled,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
            }
        }

        // Calendar Grid
        var cellIndex = 0
        while (cellIndex < totalGridCells) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                for (i in 0 until 7) {
                    val dayNumber = cellIndex - firstDayOfWeek + 1
                    val isCurrentMonth = dayNumber in 1..daysInMonth

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isCurrentMonth) {
                            val date = LocalDate(today.year, today.month, dayNumber)
                            val dateSeed =
                                date.year * 10000L + (date.month.number) * 100L + date.day
                            val isCompleted = completedDates.contains(dateSeed)
                            val isToday = date == today

                            Box(
                                modifier = Modifier
                                    .size(28.dp.scaled)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isCompleted -> MaterialTheme.colorScheme.primary.copy(
                                                alpha = 0.2f,
                                            )

                                            isToday -> Color.White.copy(alpha = 0.1f)
                                            else -> Color.Transparent
                                        },
                                    )
                                    .then(
                                        if (isToday) Modifier.border(
                                            1.dp.scaled,
                                            MaterialTheme.colorScheme.primary,
                                            CircleShape,
                                        )
                                        else if (isCompleted) Modifier.border(
                                            1.dp.scaled,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                            CircleShape,
                                        )
                                        else Modifier,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (isCompleted) {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_star),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp.scaled),
                                    )
                                } else {
                                    Text(
                                        text = dayNumber.toString(),
                                        color = if (isToday) MaterialTheme.colorScheme.primary else Color.White.copy(
                                            alpha = 0.6f,
                                        ),
                                        fontSize = 11.sp.scaled,
                                        fontWeight = if (isToday) FontWeight.Black else FontWeight.Bold,
                                    )
                                }
                            }
                        }
                    }
                    cellIndex++
                }
            }
        }
    }
}

@Composable
private fun NextRewardCard(
    streak: Int,
    nextMilestone: Int,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.spacing
    val shape = RoundedCornerShape(MaterialTheme.cornerRadius.medium.scaled)
    val reward = StreakMilestones.getRewardForStreak(nextMilestone)
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.background)
            .border(1.dp.scaled, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape)
            .padding(spacing.large.scaled),
        verticalArrangement = Arrangement.spacedBy(spacing.medium.scaled),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "NEXT MILESTONE: DAY $nextMilestone",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp.scaled,
                    letterSpacing = 1.sp.scaled,
                )
                Text(
                    text = "Keep your streak going to unlock!",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp.scaled,
                    fontWeight = FontWeight.Medium,
                )
            }

            // Streak Progress Badge
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .padding(
                        horizontal = spacing.medium.scaled,
                        vertical = spacing.extraSmall.scaled,
                    ),
            ) {
                Text(
                    text = "$streak / $nextMilestone",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp.scaled,
                )
            }
        }

        if (reward != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.medium.scaled),
            ) {
                if (reward.diamonds > 0) {
                    RewardItem(
                        icon = Res.drawable.ic_diamond,
                        label = "${reward.diamonds} Diamonds",
                        color = Color(0xFFFFD54F),
                        modifier = Modifier.weight(1f),
                    )
                }

                reward.perkRewards.forEach { (category, count) ->
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
                    RewardItem(
                        icon = icon,
                        label = "$count ${category.name} Perk",
                        color = color,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun RewardItem(
    icon: org.jetbrains.compose.resources.DrawableResource,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.spacing
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp.scaled))
            .background(Color.White.copy(alpha = 0.03f))
            .padding(spacing.small.scaled),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.small.scaled),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp.scaled),
        )
        Text(
            text = label.uppercase(),
            color = Color.White.copy(alpha = 0.8f),
            fontWeight = FontWeight.Black,
            fontSize = 9.sp.scaled,
            letterSpacing = 0.5.sp.scaled,
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
