package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.model.Perk

@Composable
fun PerkBar(
    collectedPerks: List<Perk>,
    activePerk: Perk?,
    onPerkClick: (Perk) -> Unit,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "perk_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .background(Color(0xFF1C1C24), RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .border(
                1.dp,
                Color.White.copy(alpha = 0.08f),
                RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            )
            .then(
                if (collectedPerks.isNotEmpty()) {
                    Modifier.drawBehind {
                        val baseColor = Color(0xFFF06292)
                        val cornerRadius = 32.dp.toPx()
                        
                        val path = Path().apply {
                            moveTo(0f, size.height)
                            lineTo(0f, cornerRadius)
                            arcTo(
                                rect = Rect(0f, 0f, cornerRadius * 2, cornerRadius * 2),
                                startAngleDegrees = 180f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                            lineTo(size.width - cornerRadius, 0f)
                            arcTo(
                                rect = Rect(size.width - cornerRadius * 2, 0f, size.width, cornerRadius * 2),
                                startAngleDegrees = 270f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                            lineTo(size.width, size.height)
                        }

                        // Layered strokes to create a very subtle soft glow effect
                        for (i in 1..3) {
                            val alpha = (glowAlpha / (i * 3f))
                            drawPath(
                                path = path,
                                color = baseColor.copy(alpha = alpha),
                                style = Stroke(width = (i * 3).dp.toPx())
                            )
                        }
                        
                        // Subtle inner edge
                        drawPath(
                            path = path,
                            color = baseColor.copy(alpha = glowAlpha * 0.5f),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                } else Modifier
            )
            .graphicsLayer { clip = false }
            .navigationBarsPadding()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (collectedPerks.isEmpty()) {
            Text(
                text = "Level up to collect perks and enhance your strategy!",
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(16.dp),
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                collectedPerks.distinct().forEach { perk ->
                    val count = collectedPerks.count { it == perk }
                    val isActive = activePerk == perk

                    PerkButton(
                        perk = perk,
                        count = count,
                        isActive = isActive,
                        onClick = { onPerkClick(perk) },
                    )

                    Spacer(Modifier.size(12.dp))
                }
            }
        }
    }
}

@Composable
private fun PerkButton(
    perk: Perk,
    count: Int,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 2.dp),
    ) {
        Box(
            modifier = Modifier.size(width = 54.dp, height = (54 * 0.866f).dp),
            contentAlignment = Alignment.Center,
        ) {
            // Main Hexagon Body
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(FlatTopHexagonShape())
                    .background(
                        if (isActive) Color(0xFFF06292).copy(alpha = 0.3f) else Color(0xFF2A2A36),
                    )
                    .border(
                        width = if (isActive) 2.dp else 1.dp,
                        color = if (isActive) Color(0xFFF06292) else Color.White.copy(alpha = 0.1f),
                        shape = FlatTopHexagonShape(),
                    )
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center,
            ) {
                PerkIcon(
                    perk = perk,
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            }

            // Badge counter - Moved outside the body and positioned absolutely to prevent clipping
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-2).dp)
                    .size(20.dp)
                    .background(Color(0xFFF06292), CircleShape)
                    .border(1.dp, Color(0xFF1C1C24), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = count.toString(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = perk.displayName,
            color = if (isActive) Color(0xFFF06292) else Color.White.copy(alpha = 0.6f),
            fontSize = 8.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            lineHeight = 10.sp,
            modifier = Modifier.width(60.dp),
        )
    }
}
