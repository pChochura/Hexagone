package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.pointlessgames.hexagone.achievements.GameAchievement
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.spacing
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun AchievementNotification(
    achievement: GameAchievement,
    onClick: () -> Unit,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(achievement) {
        isVisible = true
        delay(3000.milliseconds)
        isVisible = false
        delay(500.milliseconds)
        onFinished()
    }

    Popup(
        alignment = Alignment.TopCenter,
        properties = PopupProperties(focusable = false)
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(MaterialTheme.spacing.medium),
            contentAlignment = Alignment.TopCenter
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .shadow(8.dp, RoundedCornerShape(MaterialTheme.cornerRadius.medium))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF2C3E50),
                                    Color(0xFF4CA1AF)
                                )
                            ),
                            RoundedCornerShape(MaterialTheme.cornerRadius.medium)
                        )
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.2f),
                            RoundedCornerShape(MaterialTheme.cornerRadius.medium)
                        )
                        .clickable { onClick() }
                        .padding(MaterialTheme.spacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⭐",
                            fontSize = 20.sp
                        )
                    }

                    Spacer(Modifier.width(MaterialTheme.spacing.medium))

                    Column {
                        Text(
                            text = "Achievement Unlocked!",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = achievement.title,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}
