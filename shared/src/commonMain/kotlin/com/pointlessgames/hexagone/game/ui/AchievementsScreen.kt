package com.pointlessgames.hexagone.game.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.LocalNavigator
import com.pointlessgames.hexagone.achievements.AchievementManager
import com.pointlessgames.hexagone.achievements.AchievementStatus
import com.pointlessgames.hexagone.game.ui.components.ScreenScaffold
import com.pointlessgames.hexagone.game.ui.components.SectionTitle
import com.pointlessgames.hexagone.game.ui.components.WavyProgressBar
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.scaled
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.achievements_title
import hexagone.shared.generated.resources.ic_locked
import hexagone.shared.generated.resources.ic_star
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun AchievementsScreen(
    achievementManager: AchievementManager,
    initialAchievementId: String? = null,
) {
    val navigator = LocalNavigator.current
    val spacing = MaterialTheme.spacing
    val listState = rememberLazyListState()
    var statuses by remember { mutableStateOf<List<AchievementStatus>>(emptyList()) }

    LaunchedEffect(Unit) {
        statuses = achievementManager.getAchievementsStatus()
    }

    val groupedStatuses = remember(statuses) {
        statuses.groupBy { it.achievement.category }
    }

    LaunchedEffect(groupedStatuses, initialAchievementId) {
        if (initialAchievementId != null && groupedStatuses.isNotEmpty()) {
            var targetIndex = 0
            var found = false

            outer@ for (category in groupedStatuses.keys) {
                targetIndex++ // Header
                val itemsInCategory = groupedStatuses[category] ?: emptyList()
                for (status in itemsInCategory) {
                    if (status.achievement.id == initialAchievementId) {
                        found = true
                        break@outer
                    }
                    targetIndex++
                }
            }

            if (found) {
                delay(300.milliseconds)
                listState.animateScrollToItem(targetIndex)
            }
        }
    }

    ScreenScaffold(
        title = stringResource(Res.string.achievements_title),
        onBack = { navigator.pop() },
    ) { contentPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.small.scaled),
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding(),
                bottom = spacing.extraLarge.scaled,
            ),
        ) {
            groupedStatuses.forEach { (category, categoryStatuses) ->
                item {
                    Box(modifier = Modifier.padding(horizontal = spacing.extraLarge.scaled)) {
                        SectionTitle(text = stringResource(category.title))
                    }
                }

                items(categoryStatuses) { status ->
                    Box(modifier = Modifier.padding(horizontal = spacing.extraLarge.scaled)) {
                        AchievementItem(
                            status = status,
                            isHighlighted = status.achievement.id == initialAchievementId,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun AchievementItem(
    status: AchievementStatus,
    isHighlighted: Boolean = false,
) {
    val alpha = if (status.isUnlocked) 1f else 0.4f
    val spacing = MaterialTheme.spacing

    val highlightAlpha by if (isHighlighted) {
        val infiniteTransition = rememberInfiniteTransition(label = "highlight_pulse")
        infiniteTransition.animateFloat(
            initialValue = 0.1f,
            targetValue = 0.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "alpha",
        )
    } else remember { mutableStateOf(0.05f) }

    val progress =
        if (status.maxProgress > 0L) (status.currentProgress.toFloat() / status.maxProgress.toFloat()).coerceIn(
            0f,
            1f,
        ) else if (status.isUnlocked) 1f else 0f
    val shape = RoundedCornerShape(MaterialTheme.cornerRadius.medium.scaled)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(shape)
            .background(MaterialTheme.colorScheme.background)
            .border(
                1.dp.scaled,
                if (isHighlighted) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                else if (status.isUnlocked) Color.White.copy(alpha = 0.1f)
                else Color.White.copy(alpha = 0.05f),
                shape,
            ),
    ) {
        if (progress > 0f) {
            WavyProgressBar(
                progress = progress,
                modifier = Modifier.matchParentSize(),
                showContainer = true,
                containerColor = if (status.isUnlocked) Color.White.copy(alpha = 0.05f) else Color.Transparent,
                borderColor = Color.Transparent,
                shape = shape,
                isWavy = !status.isUnlocked,
            )
        } else if (isHighlighted) {
            Box(
                modifier = Modifier.matchParentSize()
                    .background(Color.White.copy(alpha = highlightAlpha)),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.large.scaled),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp.scaled)
                    .clip(CircleShape)
                    .background(
                        if (status.isUnlocked) {
                            Brush.linearGradient(listOf(Color(0xFF2C3E50), Color(0xFF4CA1AF)))
                        } else {
                            Brush.linearGradient(
                                listOf(
                                    Color.Gray.copy(alpha = 0.2f),
                                    Color.Gray.copy(alpha = 0.1f),
                                ),
                            )
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(if (status.isUnlocked) Res.drawable.ic_star else Res.drawable.ic_locked),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp.scaled),
                    tint = if (status.isUnlocked) Color.Unspecified else Color.White.copy(alpha = 0.3f),
                )
            }

            Spacer(Modifier.width(spacing.medium.scaled))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(status.achievement.title),
                    color = Color.White.copy(alpha = alpha),
                    fontSize = 16.sp.scaled,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(status.achievement.description),
                    color = Color.White.copy(alpha = alpha * 0.7f),
                    fontSize = 12.sp.scaled,
                )
            }
        }

        if (status.maxProgress > 0 && !status.isUnlocked) {
            Text(
                text = "${status.currentProgress} / ${status.maxProgress}",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 9.sp.scaled,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(spacing.small.scaled),
            )
        }
    }
}
