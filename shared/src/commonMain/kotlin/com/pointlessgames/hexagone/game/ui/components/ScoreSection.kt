package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.game.model.PreviewCell
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ScoreSection(
    score: Int,
    bestScore: Int,
    combo: Int,
    previewState: List<PreviewCell>,
    activePerk: Perk?,
    selectedCellId: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header with Game Name and Icons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* TODO */ }) {
                Canvas(modifier = Modifier.size(24.dp)) {
                    val barWidth = size.width * 0.2f
                    val spacing = size.width * 0.1f
                    drawRect(
                        color = Color.White.copy(alpha = 0.7f),
                        topLeft = Offset(0f, size.height * 0.6f),
                        size = Size(barWidth, size.height * 0.4f)
                    )
                    drawRect(
                        color = Color.White.copy(alpha = 0.7f),
                        topLeft = Offset(barWidth + spacing, size.height * 0.2f),
                        size = Size(barWidth, size.height * 0.8f)
                    )
                    drawRect(
                        color = Color.White.copy(alpha = 0.7f),
                        topLeft = Offset((barWidth + spacing) * 2, size.height * 0.4f),
                        size = Size(barWidth, size.height * 0.6f)
                    )
                }
            }

            Text(
                text = "HEXAGONE",
                color = Color(0xFFC5CAE9),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                letterSpacing = 2.sp
            )

            IconButton(onClick = { /* TODO */ }) {
                Canvas(modifier = Modifier.size(24.dp)) {
                    val outerRadius = size.minDimension / 2.5f
                    val innerRadius = size.minDimension / 5f
                    drawCircle(
                        color = Color.White.copy(alpha = 0.7f),
                        radius = outerRadius,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.7f),
                        radius = innerRadius
                    )
                    // Gear teeth
                    for (i in 0 until 8) {
                        val angle = i * (2 * PI / 8).toFloat()
                        val start = center + Offset(cos(angle) * outerRadius, sin(angle) * outerRadius)
                        val end = center + Offset(cos(angle) * (outerRadius + 4.dp.toPx()), sin(angle) * (outerRadius + 4.dp.toPx()))
                        drawLine(
                            color = Color.White.copy(alpha = 0.7f),
                            start = start,
                            end = end,
                            strokeWidth = 3.dp.toPx()
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Unified Score Section with Next Piece Integrated
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1C1C24), RoundedCornerShape(24.dp))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "SCORE",
                    color = Color.White.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )

                Box(contentAlignment = Alignment.Center, modifier = Modifier.wrapContentHeight()) {
                    Text(
                        text = score.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 48.sp,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.width(130.dp))

                        AnimatedVisibility(
                            visible = combo > 0,
                            enter = fadeIn() + scaleIn(initialScale = 0.5f),
                            exit = fadeOut() + scaleOut(targetScale = 0.5f)
                        ) {
                            Text(
                                text = "x${combo + 1}",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "BEST ",
                        color = Color.White.copy(alpha = 0.3f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                    )
                    Text(
                        text = bestScore.toString(),
                        color = Color(0xFFF06292),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                    )
                }
            }

            // Integrated Next Piece Section (Positioned at the end and smaller)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(60.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = activePerk,
                    transitionSpec = {
                        (fadeIn() togetherWith fadeOut()).using(SizeTransform(clip = false))
                    },
                    label = "integrated_next_piece"
                ) { perk ->
                    if (perk != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "PERK",
                                color = Color(0xFFF06292),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                            )
                            Text(
                                text = when (perk) {
                                    Perk.MOVE_TILE -> if (selectedCellId == null) "Select" else "Move"
                                    Perk.REMOVE_TILE -> "Pick"
                                    Perk.SWAP_TILES -> if (selectedCellId == null) "First" else "Second"
                                    else -> "Fuse"
                                },
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 12.sp
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "NEXT",
                                color = Color.White.copy(alpha = 0.3f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                            )
                            Spacer(Modifier.height(4.dp))
                            val nextValue = previewState.firstOrNull()?.value ?: 1
                            Hexagon(
                                value = nextValue.toString(),
                                backgroundColor = HexagonGridDefaults.getColorForValue(nextValue),
                                modifier = Modifier.size(28.dp).aspectRatio(1 / 0.866f),
                            )
                        }
                    }
                }
            }
        }
    }
}
