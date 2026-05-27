package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
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
    scoreProvider: () -> Int,
    bestScore: Int,
    level: Int,
    maxCombo: Int,
    totalMerges: Int,
    highestValue: Int,
    showBoard: Boolean,
    perkOptions: List<Perk>,
    pendingLevelUps: Int,
    onPerkSelected: (Perk) -> Unit,
    onRestart: () -> Unit,
    onViewBoardToggle: () -> Unit,
    onShare: () -> Unit,
    onLeaderboard: () -> Unit
) {
    val isAnyOverlayVisible = perkOptions.isNotEmpty() || isGameOver
    val dimAlphaState = animateFloatAsState(
        targetValue = if (isAnyOverlayVisible && !showBoard) 0.8f else 0f,
        animationSpec = tween(500),
        label = "dim_alpha"
    )

    if (isAnyOverlayVisible || dimAlphaState.value > 0f) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = dimAlphaState.value }
                    .background(Color.Black)
                    .clickable(
                        enabled = isAnyOverlayVisible && !showBoard,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Consume clicks */ }
            )

            AnimatedVisibility(
                visible = perkOptions.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn() + scaleIn(initialScale = 0.9f),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                PerkSelectionDialog(
                    options = perkOptions,
                    pendingLevelUps = pendingLevelUps,
                    onPerkSelected = onPerkSelected,
                )
            }

            AnimatedVisibility(
                visible = isGameOver && !showBoard,
                enter = fadeIn(tween(1000)) + slideInVertically(initialOffsetY = { it / 4 }),
                exit = fadeOut(tween(500)) + scaleOut(targetScale = 0.8f),
                modifier = Modifier.align(Alignment.Center)
            ) {
                GameOverDialog(
                    score = scoreProvider(),
                    bestScore = bestScore,
                    level = level,
                    maxCombo = maxCombo,
                    totalMerges = totalMerges,
                    highestValue = highestValue,
                    onRestart = onRestart,
                    onViewBoard = onViewBoardToggle,
                    onShare = onShare,
                    onLeaderboard = onLeaderboard
                )
            }

            if (isGameOver && showBoard) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(16.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { onViewBoardToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("✕", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
private fun PerkSelectionDialog(
    options: List<Perk>,
    pendingLevelUps: Int,
    onPerkSelected: (Perk) -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "levelup_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
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

            Row(verticalAlignment = Alignment.CenterVertically) {
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

                if (pendingLevelUps > 1) {
                    Spacer(Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFF06292), CircleShape)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "+$pendingLevelUps",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Choose your perk",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(32.dp))

            AnimatedContent(
                targetState = options,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.8f))
                        .togetherWith(fadeOut(animationSpec = tween(400)) + scaleOut(targetScale = 0.8f))
                        .using(SizeTransform(clip = false))
                },
                label = "perk_refresh"
            ) { perkOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    perkOptions.forEach { perk ->
                        PerkButton(
                            perk = perk,
                            onClick = { onPerkSelected(perk) },
                            modifier = Modifier.weight(1f),
                            showDescription = true,
                            showDropRate = true,
                            buttonSize = 64.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GameOverDialog(
    score: Int,
    bestScore: Int,
    level: Int,
    maxCombo: Int,
    totalMerges: Int,
    highestValue: Int,
    onRestart: () -> Unit,
    onViewBoard: () -> Unit,
    onShare: () -> Unit,
    onLeaderboard: () -> Unit
) {
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(2000),
        label = "score_count_up"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "gameover_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .drawBehind {
                val baseColor = Color(0xFFF06292)
                val cornerRadius = 32.dp.toPx()
                val path = Path().apply {
                    addRoundRect(RoundRect(Rect(0f, 0f, size.width, size.height), cornerRadius, cornerRadius))
                }

                // Layered strokes for glow
                for (i in 1..3) {
                    drawPath(
                        path = path,
                        color = baseColor.copy(alpha = glowAlpha / (i * 2f)),
                        style = Stroke(width = (i * 4).dp.toPx())
                    )
                }
            }
            .background(Color(0xFF1C1C24).copy(alpha = 0.98f), RoundedCornerShape(32.dp))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "GAME OVER",
                color = Color.White.copy(alpha = 0.4f),
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                letterSpacing = 6.sp
            )

            Spacer(Modifier.height(24.dp))

            Box(contentAlignment = Alignment.Center) {
                // Score Glow Effect
                Box(
                    modifier = Modifier
                        .size(200.dp, 100.dp)
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFF06292).copy(alpha = 0.15f * glowAlpha * 5f), Color.Transparent)
                                ),
                                radius = 100.dp.toPx()
                            )
                        }
                )

                Text(
                    text = animatedScore.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 72.sp,
                    textAlign = TextAlign.Center
                )
                
                if (score >= bestScore && score > 0) {
                    Box(
                        modifier = Modifier
                            .offset(y = (-45).dp)
                            .background(Color(0xFFF06292), RoundedCornerShape(100.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("NEW BEST", color = Color.White, fontWeight = FontWeight.Black, fontSize = 9.sp)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Stats Grid (2x2)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GameOverStatBox("LEVEL", level.toString(), modifier = Modifier.weight(1f))
                    GameOverStatBox("MERGES", totalMerges.toString(), modifier = Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GameOverStatBox("MAX COMBO", "x$maxCombo", modifier = Modifier.weight(1f))
                    GameOverStatBox("MAX PIECE", highestValue.toString(), isHex = true, modifier = Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(40.dp))

            // Main Primary Action
            Button(
                onClick = onRestart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF06292),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "PLAY AGAIN",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            // Secondary Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SecondaryGameButton(
                    onClick = onShare,
                    icon = "⤴",
                    modifier = Modifier.weight(1f)
                )
                SecondaryGameButton(
                    onClick = onLeaderboard,
                    icon = "🏆",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // View Board
            Text(
                text = "VIEW BOARD",
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onViewBoard
                    )
                    .padding(8.dp)
            )
        }
    }
}

@Composable
private fun GameOverStatBox(
    label: String, 
    value: String, 
    modifier: Modifier = Modifier,
    isHex: Boolean = false
) {
    Box(
        modifier = modifier
            .height(64.dp)
            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.3f),
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                letterSpacing = 0.5.sp
            )
            Spacer(Modifier.height(2.dp))
            if (isHex) {
                val valInt = value.toIntOrNull() ?: 1
                Hexagon(
                    value = value,
                    backgroundColor = HexagonGridDefaults.getColorForValue(valInt).copy(alpha = 0.2f),
                    isOutline = true,
                    modifier = Modifier.size(24.dp).aspectRatio(1 / 0.866f)
                )
            } else {
                Text(
                    text = value,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
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
                    horizontalArrangement = Arrangement.Center,
                ) {
                    displayPerks.forEach { perk ->
                        val count = collectedPerks.count { it == perk }
                        PerkButton(
                            perk = perk,
                            onClick = { onUsePerk(perk) },
                            count = count,
                            buttonSize = 54.dp
                        )
                        Spacer(Modifier.width(12.dp))
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

@Composable
private fun SecondaryGameButton(
    onClick: () -> Unit,
    icon: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = icon, color = Color.White, fontSize = 20.sp)
    }
}
