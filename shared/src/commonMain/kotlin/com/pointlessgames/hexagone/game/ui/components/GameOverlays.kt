package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = perkOptions.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            PerkSelectionDialog(
                options = perkOptions,
                onPerkSelected = onPerkSelected,
            )
        }

        AnimatedVisibility(
            visible = (isStuck || isGameOver) && perkOptions.isEmpty(),
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
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
                .padding(16.dp)
                .fillMaxWidth()
                .background(Color(0xFF1C1C24), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.1f),
                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "LEVEL UP!",
                color = Color(0xFFF06292),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Choose your perk",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                options.forEach { perk ->
                    Button(
                        onClick = { onPerkSelected(perk) },
                        modifier = Modifier.weight(1f).height(100.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2A2A36),
                            contentColor = Color.White,
                        ),
                        contentPadding = PaddingValues(8.dp),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                perk.displayName.split(" ").first(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                perk.description,
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
                .padding(16.dp)
                .fillMaxWidth()
                .background(Color(0xFF1C1C24), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.1f),
                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
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
