package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Color(0xFF1C1C24), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .border(
                1.dp,
                Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .padding(horizontal = 16.dp)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        if (collectedPerks.isEmpty()) {
            Text(
                text = "Level up to collect perks and enhance your strategy!",
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 32.dp)
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

                    Spacer(Modifier.size(16.dp))
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
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    if (isActive) Color(0xFFF06292).copy(alpha = 0.2f) else Color(0xFF2A2A36),
                    CircleShape,
                )
                .border(
                    width = if (isActive) 2.dp else 1.dp,
                    color = if (isActive) Color(0xFFF06292) else Color.White.copy(alpha = 0.1f),
                    shape = CircleShape,
                )
                .clip(CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(24.dp)) {
                val strokeWidth = 2.dp.toPx()
                when (perk) {
                    Perk.ADVANCE_QUEUE -> {
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(0f, 0f)
                            lineTo(size.width * 0.5f, size.height * 0.5f)
                            lineTo(0f, size.height)
                            moveTo(size.width * 0.5f, 0f)
                            lineTo(size.width, size.height * 0.5f)
                            lineTo(size.width * 0.5f, size.height)
                        }
                        drawPath(path, color = Color.White, style = Stroke(width = strokeWidth))
                    }

                    Perk.MOVE_TILE -> {
                        drawLine(
                            Color.White,
                            Offset(size.width * 0.5f, 0f),
                            Offset(size.width * 0.5f, size.height),
                            strokeWidth,
                        )
                        drawLine(
                            Color.White,
                            Offset(0f, size.height * 0.5f),
                            Offset(size.width, size.height * 0.5f),
                            strokeWidth,
                        )
                    }

                    Perk.REMOVE_TILE -> {
                        drawRect(
                            Color.White,
                            Offset(size.width * 0.2f, size.height * 0.3f),
                            size.copy(width = size.width * 0.6f, height = size.height * 0.6f),
                            style = Stroke(width = strokeWidth),
                        )
                        drawLine(
                            Color.White,
                            Offset(size.width * 0.1f, size.height * 0.2f),
                            Offset(size.width * 0.9f, size.height * 0.2f),
                            strokeWidth,
                        )
                    }

                    Perk.FUSION -> {
                        drawCircle(
                            Color.White,
                            radius = size.minDimension * 0.4f,
                            style = Stroke(width = strokeWidth),
                        )
                        drawCircle(Color.White, radius = size.minDimension * 0.2f)
                    }

                    Perk.SWAP_TILES -> {
                        // Two horizontal arrows in opposite directions
                        val arrowSize = size.width * 0.3f
                        // Top arrow (right)
                        drawLine(Color.White, Offset(0f, size.height * 0.3f), Offset(size.width, size.height * 0.3f), strokeWidth)
                        drawLine(Color.White, Offset(size.width, size.height * 0.3f), Offset(size.width - arrowSize, size.height * 0.15f), strokeWidth)
                        drawLine(Color.White, Offset(size.width, size.height * 0.3f), Offset(size.width - arrowSize, size.height * 0.45f), strokeWidth)
                        
                        // Bottom arrow (left)
                        drawLine(Color.White, Offset(0f, size.height * 0.7f), Offset(size.width, size.height * 0.7f), strokeWidth)
                        drawLine(Color.White, Offset(0f, size.height * 0.7f), Offset(arrowSize, size.height * 0.55f), strokeWidth)
                        drawLine(Color.White, Offset(0f, size.height * 0.7f), Offset(arrowSize, size.height * 0.85f), strokeWidth)
                    }

                    Perk.CHAIN_MERGE -> {
                        // Drawing a chain link icon
                        drawCircle(
                            Color.White,
                            radius = size.width * 0.15f,
                            center = Offset(size.width * 0.25f, size.height * 0.25f),
                            style = Stroke(width = strokeWidth)
                        )
                        drawLine(
                            Color.White,
                            Offset(size.width * 0.35f, size.height * 0.35f),
                            Offset(size.width * 0.65f, size.height * 0.65f),
                            strokeWidth
                        )
                        drawCircle(
                            Color.White,
                            radius = size.width * 0.15f,
                            center = Offset(size.width * 0.75f, size.height * 0.75f),
                            style = Stroke(width = strokeWidth)
                        )
                    }

                    Perk.UNDO -> {
                        // Anti-clockwise arrow
                        drawArc(
                            color = Color.White,
                            startAngle = 0f,
                            sweepAngle = 270f,
                            useCenter = false,
                            topLeft = Offset(size.width * 0.1f, size.height * 0.1f),
                            size = size.copy(width = size.width * 0.8f, height = size.height * 0.8f),
                            style = Stroke(width = strokeWidth)
                        )
                        val arrowSize = size.width * 0.2f
                        drawLine(
                            Color.White,
                            Offset(size.width * 0.1f, size.height * 0.5f),
                            Offset(size.width * 0.1f - arrowSize, size.height * 0.5f - arrowSize),
                            strokeWidth
                        )
                        drawLine(
                            Color.White,
                            Offset(size.width * 0.1f, size.height * 0.5f),
                            Offset(size.width * 0.1f + arrowSize, size.height * 0.5f - arrowSize),
                            strokeWidth
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .background(Color(0xFFF06292), CircleShape)
                    .size(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = count.toString(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            perk.displayName.split(" ").first(),
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
