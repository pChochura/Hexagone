package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ScoreSection(
    score: Int,
    bestScore: Int,
    combo: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ... (Header remains same) ...
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

        // Unified Score Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1C1C24), RoundedCornerShape(24.dp))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                .padding(vertical = 16.dp),
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
            
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = score.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 48.sp,
                    textAlign = TextAlign.Center
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.width(120.dp)) // Reserve space for the score
                    
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
    }
}
