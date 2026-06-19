package com.pointlessgames.hexagone.game.ui

import androidx.compose.animation.core.EaseInOutSine
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
import androidx.compose.foundation.text.TextAutoSize
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawOutline
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
import com.pointlessgames.hexagone.game.ui.components.SectionTitle
import com.pointlessgames.hexagone.game.ui.components.displayNameRes
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.scaled
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.active_missions_title
import hexagone.shared.generated.resources.bonus_reward_prefix
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
import hexagone.shared.generated.resources.daily_completion_reward_hint
import hexagone.shared.generated.resources.daily_completion_reward_title
import hexagone.shared.generated.resources.daily_login_diamond_claimed
import hexagone.shared.generated.resources.daily_login_diamond_unclaimed
import hexagone.shared.generated.resources.daily_login_reward_title
import hexagone.shared.generated.resources.day_fri
import hexagone.shared.generated.resources.day_mon
import hexagone.shared.generated.resources.day_sat
import hexagone.shared.generated.resources.day_sun
import hexagone.shared.generated.resources.day_thu
import hexagone.shared.generated.resources.day_tue
import hexagone.shared.generated.resources.day_wed
import hexagone.shared.generated.resources.ic_daily_challenge
import hexagone.shared.generated.resources.ic_diamond
import hexagone.shared.generated.resources.ic_help
import hexagone.shared.generated.resources.ic_legendary_perk
import hexagone.shared.generated.resources.ic_rare_perk
import hexagone.shared.generated.resources.ic_roll
import hexagone.shared.generated.resources.ic_star
import hexagone.shared.generated.resources.mission_complete_badge
import hexagone.shared.generated.resources.mission_log_title
import hexagone.shared.generated.resources.pattern_great_wall
import hexagone.shared.generated.resources.pattern_ring_of_fire
import hexagone.shared.generated.resources.pattern_the_prism
import hexagone.shared.generated.resources.pattern_twin_peaks
import hexagone.shared.generated.resources.perk_category_common
import hexagone.shared.generated.resources.perk_category_legendary
import hexagone.shared.generated.resources.perk_category_rare
import hexagone.shared.generated.resources.previous_missions_warning
import hexagone.shared.generated.resources.progress_fraction
import hexagone.shared.generated.resources.reward_perk_label
import hexagone.shared.generated.resources.reward_score_label
import hexagone.shared.generated.resources.streak_explanation
import hexagone.shared.generated.resources.streak_label
import hexagone.shared.generated.resources.streak_milestone_diamonds
import hexagone.shared.generated.resources.streak_milestone_perks
import hexagone.shared.generated.resources.streak_motivation
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.DrawableResource
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
            val isPreviousMissions =
                uiState.dailyMissionDate != 0L && uiState.dailyMissionDate < (today.year * 10000L + (today.month.ordinal + 1) * 100L + today.day)

            if (isPreviousMissions) {
                item {
                    Spacer(Modifier.height(spacing.small.scaled))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.extraLarge.scaled)
                            .clip(RoundedCornerShape(MaterialTheme.cornerRadius.medium.scaled))
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                            .border(
                                1.dp.scaled,
                                MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                                RoundedCornerShape(MaterialTheme.cornerRadius.medium.scaled),
                            )
                            .padding(spacing.medium.scaled),
                    ) {
                        Text(
                            text = stringResource(Res.string.previous_missions_warning),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp.scaled,
                            lineHeight = 16.sp.scaled,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            // Prominent Streak Section (Top)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = spacing.extraLarge.scaled,
                            vertical = spacing.medium.scaled,
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacing.extraSmall.scaled),
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
                        text = stringResource(Res.string.streak_motivation).uppercase(),
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp.scaled,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp.scaled,
                    )
                }
            }

            // Today's Completion Reward Card
            item {
                TodayCompletionRewardCard(
                    streak = uiState.challengeStreak,
                    modifier = Modifier.padding(horizontal = spacing.extraLarge.scaled),
                )
            }

            // Daily Login Reward Banner
            item {
                DailyLoginRewardBanner(
                    isClaimed = uiState.isDailyLoginClaimed,
                    modifier = Modifier.padding(horizontal = spacing.extraLarge.scaled),
                )
            }

            item {
                Column {
                    SectionTitle(
                        text = stringResource(Res.string.active_missions_title),
                        modifier = Modifier.padding(horizontal = spacing.extraLarge.scaled),
                    )

                    HelpRow()
                    Spacer(Modifier.height(spacing.medium.scaled))
                }
            }

            // Active Missions
            items(uiState.dailyChallenges) { progress ->
                Box(modifier = Modifier.padding(horizontal = spacing.extraLarge.scaled)) {
                    ChallengeCard(
                        progress = progress,
                        isPersistentCompleted = uiState.persistentCompletedMissionIds.contains(
                            progress.challenge.id,
                        ),
                    )
                }
            }

            // Calendar Section (Bottom)
            item {
                SectionTitle(
                    text = stringResource(Res.string.mission_log_title),
                    modifier = Modifier.padding(horizontal = spacing.extraLarge.scaled),
                )
            }

            item {
                HexCalendar(
                    today = today,
                    completedDates = uiState.completedChallengeDates,
                    modifier = Modifier.padding(horizontal = spacing.extraLarge.scaled),
                )
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
            listOf(
                Res.string.day_mon,
                Res.string.day_tue,
                Res.string.day_wed,
                Res.string.day_thu,
                Res.string.day_fri,
                Res.string.day_sat,
                Res.string.day_sun,
            ).forEach { day ->
                Text(
                    text = stringResource(day),
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
                                        textAlign = TextAlign.Center,
                                        autoSize = TextAutoSize.StepBased(
                                            minFontSize = 7.sp.scaled,
                                            maxFontSize = 11.sp.scaled,
                                        ),
                                        lineHeight = 11.sp.scaled,
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
private fun DailyLoginRewardBanner(
    isClaimed: Boolean,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.spacing
    val shape = RoundedCornerShape(MaterialTheme.cornerRadius.medium.scaled)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                if (isClaimed) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.White.copy(
                    alpha = 0.05f,
                ),
            )
            .border(
                1.dp.scaled,
                if (isClaimed) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.White.copy(
                    alpha = 0.1f,
                ),
                shape,
            )
            .padding(spacing.medium.scaled),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.medium.scaled),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp.scaled)
                    .clip(CircleShape)
                    .background(Color(0xFFFFD54F).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_diamond),
                    contentDescription = null,
                    tint = Color(0xFFFFD54F),
                    modifier = Modifier.size(16.dp.scaled),
                )
            }
            Column {
                Text(
                    text = stringResource(Res.string.daily_login_reward_title),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp.scaled,
                    letterSpacing = 1.sp.scaled,
                )
                Text(
                    text = stringResource(
                        if (isClaimed) {
                            Res.string.daily_login_diamond_claimed
                        } else {
                            Res.string.daily_login_diamond_unclaimed
                        },
                    ),
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 10.sp.scaled,
                )
            }
        }

        if (isClaimed) {
            Icon(
                painter = painterResource(Res.drawable.ic_star),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp.scaled),
            )
        }
    }
}

@Composable
private fun TodayCompletionRewardCard(
    streak: Int,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.spacing
    val shape = RoundedCornerShape(MaterialTheme.cornerRadius.medium.scaled)
    val reward = StreakMilestones.getRewardForStreak(if (streak > 0) streak else 1) ?: return

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
            Column(modifier = Modifier.weight(1f).padding(end = spacing.medium.scaled)) {
                Text(
                    text = stringResource(Res.string.daily_completion_reward_title),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp.scaled,
                    letterSpacing = 1.sp.scaled,
                )
                Text(
                    text = stringResource(Res.string.daily_completion_reward_hint),
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp.scaled,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.small.scaled),
        ) {
            if (reward.diamonds > 0) {
                RewardItem(
                    icon = Res.drawable.ic_diamond,
                    label = stringResource(
                        Res.string.streak_milestone_diamonds,
                        reward.diamonds,
                    ),
                    color = Color(0xFFFFD54F),
                    modifier = Modifier.fillMaxWidth(),
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
                    modifier = Modifier.fillMaxWidth(),
                )
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
private fun HelpRow(modifier: Modifier = Modifier) {
    val spacing = MaterialTheme.spacing
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.extraLarge.scaled),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(spacing.small.scaled),
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_help),
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.4f),
            modifier = Modifier.size(16.dp.scaled),
        )
        Text(
            text = stringResource(Res.string.streak_explanation),
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 11.sp.scaled,
            lineHeight = 16.sp.scaled,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ChallengeCard(
    progress: DailyChallengeProgress,
    isPersistentCompleted: Boolean,
) {
    val challenge = progress.challenge
    val isSessionCompleted = progress.isCompleted

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

    val infiniteTransition = rememberInfiniteTransition(label = "card_glow")
    val glowAlphaState = infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow_alpha",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(shape)
            .background(MaterialTheme.colorScheme.background)
            .then(
                if (isSessionCompleted) {
                    val primary = MaterialTheme.colorScheme.primary
                    val strokeWidth = 1.dp.scaled
                    Modifier.drawBehind {
                        val alpha = glowAlphaState.value
                        drawOutline(
                            outline = shape.createOutline(size, layoutDirection, this),
                            color = primary,
                            alpha = alpha,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth.toPx()),
                        )
                    }
                } else {
                    Modifier.border(
                        1.dp.scaled,
                        if (isPersistentCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else Color.White.copy(alpha = 0.1f),
                        shape,
                    )
                },
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progressFraction)
                .fillMaxHeight()
                .background(
                    when {
                        isSessionCompleted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        isPersistentCompleted -> Color.White.copy(alpha = 0.05f)
                        else -> Color.White.copy(alpha = 0.03f)
                    },
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
                        when {
                            isSessionCompleted -> Brush.linearGradient(
                                listOf(Color(0xFFF2994A), Color(0xFFF2C94C)),
                            )

                            isPersistentCompleted -> Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.15f),
                                    Color.White.copy(alpha = 0.1f),
                                ),
                            )

                            else -> Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.1f),
                                    Color.White.copy(alpha = 0.05f),
                                ),
                            )
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(
                        if (isSessionCompleted || isPersistentCompleted) Res.drawable.ic_star
                        else Res.drawable.ic_daily_challenge,
                    ),
                    contentDescription = null,
                    tint = when {
                        isSessionCompleted -> Color.White
                        isPersistentCompleted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        else -> Color.White.copy(alpha = 0.5f)
                    },
                    modifier = Modifier.size(20.dp.scaled),
                )
            }

            Spacer(Modifier.width(MaterialTheme.spacing.medium.scaled))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small.scaled),
                ) {
                    Text(
                        text = goalText,
                        color = when {
                            isSessionCompleted -> Color.White
                            isPersistentCompleted -> Color.White.copy(alpha = 0.9f)
                            else -> Color.White.copy(alpha = 0.8f)
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp.scaled,
                        modifier = Modifier.weight(1f),
                    )

                    if (isPersistentCompleted) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp.scaled, vertical = 3.dp.scaled),
                        ) {
                            Text(
                                text = stringResource(Res.string.mission_complete_badge),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp.scaled,
                                letterSpacing = 1.sp.scaled,
                            )
                        }
                    }
                }

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
                        text = if (isSessionCompleted) stringResource(Res.string.daily_challenge_completed)
                        else stringResource(
                            Res.string.progress_fraction,
                            progress.progress,
                            challenge.target,
                        ),
                        color = when {
                            isSessionCompleted -> MaterialTheme.colorScheme.primary
                            isPersistentCompleted -> Color.White.copy(alpha = 0.5f)
                            else -> Color.White.copy(alpha = 0.4f)
                        },
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
                            text = (if (isPersistentCompleted && !isSessionCompleted) stringResource(
                                Res.string.bonus_reward_prefix,
                                rewardText,
                            ) else rewardText).uppercase(),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = if (isSessionCompleted) 1f else 0.8f),
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
