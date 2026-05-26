package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.model.Perk
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameOverlays(
    isGameOver: Boolean,
    isStuck: Boolean,
    perkOptions: List<Perk>,
    collectedPerks: List<Perk>,
    onPerkSelected: (Perk) -> Unit,
    onUsePerk: (Perk) -> Unit,
    onRestart: () -> Unit,
) {
    val isAnyOverlayVisible = perkOptions.isNotEmpty() || (isStuck || isGameOver)
    val dimAlpha by animateFloatAsState(
        targetValue = if (isAnyOverlayVisible) 0.7f else 0f,
        animationSpec = tween(500),
        label = "dim_alpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        if (dimAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = dimAlpha))
                    .clickable(
                        enabled = isAnyOverlayVisible,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Consume clicks */ }
            )
        }

        AnimatedVisibility(
            visible = perkOptions.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn() + scaleIn(initialScale = 0.9f),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            PerkSelectionDialog(
                options = perkOptions,
                onPerkSelected = onPerkSelected,
            )
        }

        AnimatedVisibility(
            visible = (isStuck || isGameOver) && perkOptions.isEmpty(),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            StatusDialog(
                isGameOver = isGameOver,
                collectedPerks = collectedPerks,
                onUsePerk = onUsePerk,
                onRestart = onRestart,
            )
        }
    }
}

@Composable
private fun PerkSelectionDialog(
    options: List<Perk>,
    onPerkSelected: (Perk) -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "levelup_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                ),
            )
            .padding(top = 100.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1C1C24), RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .border(
                    2.dp,
                    Color(0xFFF06292).copy(alpha = 0.2f),
                    RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                )
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Drag Handle
            Box(
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .size(width = 48.dp, height = 4.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
            )

            Text(
                text = "LEVEL UP!",
                color = Color(0xFFF06292),
                fontWeight = FontWeight.Black,
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Choose your perk",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                options.forEach { perk ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 64.dp, height = (64 * 0.866f).dp)
                                .clip(FlatTopHexagonShape())
                                .background(Color(0xFF2A2A36))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), FlatTopHexagonShape())
                                .clickable { onPerkSelected(perk) },
                            contentAlignment = Alignment.Center
                        ) {
                            PerkIcon(
                                perk = perk,
                                modifier = Modifier.size(28.dp),
                                color = Color.White
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = perk.displayName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                        )
                        
                        Spacer(Modifier.height(4.dp))
                        
                        Text(
                            text = perk.description,
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 11.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusDialog(
    isGameOver: Boolean,
    collectedPerks: List<Perk>,
    onUsePerk: (Perk) -> Unit,
    onRestart: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                ),
            )
            .padding(top = 64.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1C1C24), RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.08f),
                    RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                )
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Drag Handle
            Box(
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Canvas(
                    modifier = Modifier
                        .size(48.dp)
                        .border(1.dp, Color(0xFFF06292).copy(alpha = 0.3f), CircleShape)
                        .padding(8.dp)
                        .background(Color(0xFFF06292).copy(alpha = 0.1f), CircleShape),
                ) {
                    val strokeWidth = 2.dp.toPx()
                    drawCircle(
                        color = Color(0xFFF06292),
                        radius = size.minDimension / 2.5f,
                        style = Stroke(width = strokeWidth),
                    )
                    val radius = size.minDimension / 2.5f
                    val angle = 45f * (PI.toFloat() / 180f)
                    drawLine(
                        color = Color(0xFFF06292),
                        start = center + Offset(cos(angle) * radius, sin(angle) * radius),
                        end = center - Offset(cos(angle) * radius, sin(angle) * radius),
                        strokeWidth = strokeWidth,
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column {
                    Text(
                        text = if (isGameOver) "GAME OVER" else "NO MORE MOVES",
                        color = Color(0xFFD1C4E9),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                    )
                    Text(
                        text = if (isGameOver) "Better luck next time!" else "Try using a perk.",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            val displayPerks = collectedPerks.distinct().filter { it.canSaveFromStuck }

            if (displayPerks.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    displayPerks.forEach { perk ->
                        val count = collectedPerks.count { it == perk }
                        Button(
                            onClick = { onUsePerk(perk) },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4F6BFF),
                                contentColor = Color.White,
                            ),
                            contentPadding = PaddingValues(4.dp),
                        ) {
                            Text(
                                "${perk.displayName.split(" ").first()} ($count)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            OutlinedButton(
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color.White.copy(alpha = 0.3f),
                ),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            ) {
                Text("RESTART GAME", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
