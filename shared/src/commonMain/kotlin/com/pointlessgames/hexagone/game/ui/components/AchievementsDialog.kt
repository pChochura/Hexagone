package com.pointlessgames.hexagone.game.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.achievements.AchievementManager
import com.pointlessgames.hexagone.achievements.AchievementStatus
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AchievementsDialog(
    achievementManager: AchievementManager,
    onDismiss: () -> Unit
) {
    var statuses by remember { mutableStateOf<List<AchievementStatus>>(emptyList()) }

    LaunchedEffect(Unit) {
        statuses = achievementManager.getAchievementsStatus()
    }

    val groupedStatuses = remember(statuses) {
        statuses.groupBy { it.achievement.category }
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
                        AchievementItem(status)
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
private fun AchievementItem(status: AchievementStatus) {
    val alpha = if (status.isUnlocked) 1f else 0.4f
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (status.isUnlocked) Color.White.copy(alpha = 0.05f) else Color.Transparent,
                RoundedCornerShape(MaterialTheme.cornerRadius.medium)
            )
            .border(
                1.dp,
                if (status.isUnlocked) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f),
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
        }
    }
}
