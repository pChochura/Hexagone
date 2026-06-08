package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.pointlessgames.hexagone.game.model.DailyChallenge
import com.pointlessgames.hexagone.game.model.DailyChallengeProgress
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyChallengeDialog(
    challenges: List<DailyChallengeProgress>,
    streak: Int,
    globalPoints: Int,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        contentWindowInsets = { WindowInsets.statusBars },
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.2f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.extraLarge)
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + MaterialTheme.spacing.large)
        ) {
            Text(
                text = stringResource(Res.string.daily_challenge).uppercase(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Spacer(Modifier.height(MaterialTheme.spacing.medium))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(challenges) { progress ->
                    ChallengeCard(progress)
                }

                item {
                    Spacer(Modifier.height(MaterialTheme.spacing.medium))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
                    ) {
                        InfoCard(
                            label = stringResource(Res.string.streak_label, streak),
                            icon = Res.drawable.ic_star,
                            modifier = Modifier.weight(1f)
                        )
                        InfoCard(
                            label = "$globalPoints PTS",
                            icon = Res.drawable.ic_star,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    if (streak > 0) {
                        Spacer(Modifier.height(MaterialTheme.spacing.medium))
                        Text(
                            text = stringResource(Res.string.streak_bonus_label, streak * 5),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengeCard(
    progress: DailyChallengeProgress
) {
    val challenge = progress.challenge
    val isCompleted = progress.isCompleted
    val goalText = when (challenge.goal) {
        ChallengeGoal.MERGE_COUNT -> stringResource(Res.string.daily_challenge_goal_merge, challenge.target)
        ChallengeGoal.LEVEL_REACHED -> stringResource(Res.string.daily_challenge_goal_level, challenge.target)
        ChallengeGoal.COMBO_REACHED -> stringResource(Res.string.daily_challenge_goal_combo, challenge.target)
        ChallengeGoal.SCORE_REACHED -> stringResource(Res.string.daily_challenge_goal_score, challenge.target)
        ChallengeGoal.TACTICAL_MERGES -> stringResource(Res.string.daily_challenge_goal_tactical, challenge.target)
        ChallengeGoal.PIECE_VALUE_REACHED -> stringResource(Res.string.daily_challenge_goal_value, challenge.target)
    }

    val progressFraction = (progress.progress.toFloat() / challenge.target.toFloat()).coerceIn(0f, 1f)
    val shape = RoundedCornerShape(MaterialTheme.cornerRadius.medium)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, if (isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f), shape)
            .padding(MaterialTheme.spacing.medium)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCompleted) Brush.linearGradient(listOf(Color(0xFFF2994A), Color(0xFFF2C94C)))
                        else Brush.linearGradient(listOf(Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.05f)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(if (isCompleted) Res.drawable.ic_star else Res.drawable.ic_daily_challenge),
                    contentDescription = null,
                    tint = if (isCompleted) Color.White else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(MaterialTheme.spacing.medium))

            Column {
                Text(
                    text = goalText,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = if (isCompleted) stringResource(Res.string.daily_challenge_completed) 
                           else "${progress.progress} / ${challenge.target}",
                    color = if (isCompleted) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(MaterialTheme.spacing.medium))

        LinearProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = Color.White.copy(alpha = 0.1f)
        )
    }
}

@Composable
private fun RewardItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary)
        )
        Spacer(Modifier.width(MaterialTheme.spacing.small))
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun InfoCard(
    label: String,
    icon: org.jetbrains.compose.resources.DrawableResource,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(MaterialTheme.cornerRadius.small))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(MaterialTheme.spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(MaterialTheme.spacing.small))
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}
