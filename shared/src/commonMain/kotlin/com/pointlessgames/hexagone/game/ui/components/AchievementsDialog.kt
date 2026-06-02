package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.achievements.AchievementManager
import com.pointlessgames.hexagone.achievements.AchievementStatus
import com.pointlessgames.hexagone.achievements.GameAchievement
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.spacing
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AchievementsDialog(
    achievementManager: AchievementManager,
    initialAchievement: GameAchievement? = null,
    onDismiss: () -> Unit
) {
    var statuses by remember { mutableStateOf<List<AchievementStatus>>(emptyList()) }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        statuses = achievementManager.getAchievementsStatus()
    }

    val groupedStatuses = remember(statuses) {
        statuses.groupBy { it.achievement.category }
    }

    LaunchedEffect(groupedStatuses, initialAchievement) {
        if ((initialAchievement != null) && groupedStatuses.isNotEmpty()) {
            var targetIndex = 0
            var found = false
            
            outer@for (category in groupedStatuses.keys) {
                targetIndex++ // StickyHeader
                val itemsInCategory = groupedStatuses[category] ?: emptyList()
                for (status in itemsInCategory) {
                    if (status.achievement == initialAchievement) {
                        found = true
                        break@outer
                    }
                    targetIndex++
                }
                targetIndex++ // Spacer
            }
            
            if (found) {
                delay(300.milliseconds) // Wait for sheet to expand
                listState.animateScrollToItem(targetIndex)
            }
        }
    }

    ModalBottomSheet(
        contentWindowInsets = { WindowInsets.statusBars },
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = Color.Transparent,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.2f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.extraLarge)
        ) {
            Text(
                text = "Achievements",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Spacer(Modifier.height(MaterialTheme.spacing.medium))

            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding() + MaterialTheme.spacing.large
                ),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                groupedStatuses.forEach { (category, categoryStatuses) ->
                    stickyHeader {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(vertical = MaterialTheme.spacing.small)
                        ) {
                            Text(
                                text = category.title.uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    items(categoryStatuses) { status ->
                        AchievementItem(
                            status = status,
                            isHighlighted = status.achievement == initialAchievement
                        )
                    }

                    item {
                        Spacer(Modifier.height(MaterialTheme.spacing.medium))
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementItem(
    status: AchievementStatus,
    isHighlighted: Boolean = false
) {
    val alpha = if (status.isUnlocked) 1f else 0.4f
    
    val highlightAlpha by if (isHighlighted) {
        val infiniteTransition = rememberInfiniteTransition(label = "highlight_pulse")
        infiniteTransition.animateFloat(
            initialValue = 0.1f,
            targetValue = 0.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )
    } else remember { mutableStateOf(0.05f) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (status.isUnlocked || isHighlighted) Color.White.copy(alpha = if (isHighlighted) highlightAlpha else 0.05f) else Color.Transparent,
                RoundedCornerShape(MaterialTheme.cornerRadius.medium)
            )
            .border(
                1.dp,
                if (isHighlighted) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) 
                else if (status.isUnlocked) Color.White.copy(alpha = 0.1f) 
                else Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(MaterialTheme.cornerRadius.medium)
            )
            .padding(MaterialTheme.spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (status.isUnlocked) {
                        Brush.linearGradient(listOf(Color(0xFF2C3E50), Color(0xFF4CA1AF)))
                    } else {
                        Brush.linearGradient(listOf(Color.Gray.copy(alpha = 0.2f), Color.Gray.copy(alpha = 0.1f)))
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (status.isUnlocked) "⭐" else "🔒",
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }

        Spacer(Modifier.width(MaterialTheme.spacing.medium))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = status.achievement.title,
                color = Color.White.copy(alpha = alpha),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = status.achievement.description,
                color = Color.White.copy(alpha = alpha * 0.7f),
                fontSize = 12.sp
            )

            if (status.maxProgress > 0 && !status.isUnlocked) {
                Spacer(Modifier.height(MaterialTheme.spacing.small))
                AchievementProgressBar(
                    current = status.currentProgress,
                    max = status.maxProgress
                )
            }
        }
    }
}

@Composable
private fun AchievementProgressBar(
    current: Long,
    max: Long
) {
    val progress = (current.toFloat() / max.toFloat()).coerceIn(0f, 1f)
    val colorScheme = MaterialTheme.colorScheme
    
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                .clip(RoundedCornerShape(4.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width * progress
                if (width > 0) {
                    drawRect(
                        brush = Brush.horizontalGradient(
                            listOf(
                                colorScheme.scrim.copy(alpha = 0.3f),
                                colorScheme.primary.copy(alpha = 0.6f),
                            )
                        ),
                        size = Size(width, size.height)
                    )
                }
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = "$current / $max",
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End
        )
    }
}
