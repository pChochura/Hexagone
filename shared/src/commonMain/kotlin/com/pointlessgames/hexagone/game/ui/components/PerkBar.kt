package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.perk_bar_empty_hint
import org.jetbrains.compose.resources.stringResource

@Composable
fun PerkBar(
    collectedPerks: List<Perk>,
    activePerk: Perk?,
    isStuck: Boolean,
    onPerkClick: (Perk) -> Unit,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "perk_glow")
    val glowAlphaState = infiniteTransition.animateFloat(
        initialValue = if (isStuck) 0.4f else 0.05f,
        targetValue = if (isStuck) 1.0f else 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isStuck) 600 else 2500),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow_alpha",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (collectedPerks.isNotEmpty()) {
                    Modifier.drawBehind {
                        val glowAlpha = glowAlphaState.value
                        val baseColor = Color(0xFFF06292)
                        val cornerRadius = 32.dp.toPx()

                        val path = Path().apply {
                            moveTo(0f, size.height)
                            lineTo(0f, cornerRadius)
                            arcTo(
                                rect = Rect(0f, 0f, cornerRadius * 2, cornerRadius * 2),
                                startAngleDegrees = 180f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false,
                            )
                            lineTo(size.width - cornerRadius, 0f)
                            arcTo(
                                rect = Rect(
                                    size.width - cornerRadius * 2,
                                    0f,
                                    size.width,
                                    cornerRadius * 2,
                                ),
                                startAngleDegrees = 270f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false,
                            )
                            lineTo(size.width, size.height)
                        }

                        // Layered strokes to create a very subtle soft glow effect
                        for (i in 1..3) {
                            val alpha = (glowAlpha / (i * 3f))
                            drawPath(
                                path = path,
                                color = baseColor.copy(alpha = alpha),
                                style = Stroke(width = (i * 3).dp.toPx()),
                            )
                        }

                        // Subtle inner edge
                        drawPath(
                            path = path,
                            color = baseColor.copy(alpha = glowAlpha * 0.5f),
                            style = Stroke(width = 1.dp.toPx()),
                        )
                    }
                } else Modifier,
            )
            .graphicsLayer { clip = false },
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .background(Color(0xFF1C1C24), RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.08f),
                    RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                )
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .horizontalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (collectedPerks.isEmpty()) {
                Text(
                    text = stringResource(Res.string.perk_bar_empty_hint),
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(16.dp),
                )
            } else {
                val distinctPerks = remember(collectedPerks) { collectedPerks.distinct() }
                distinctPerks.forEachIndexed { index, perk ->
                    val count = remember(collectedPerks, perk) { collectedPerks.count { it == perk } }
                    val isActive = activePerk == perk
                    val isEnabled = !isStuck || perk.canSaveFromStuck

                    PerkButton(
                        perk = perk,
                        count = count,
                        isActive = isActive,
                        isEnabled = isEnabled,
                        tooltipDescription = perk.descriptionRes,
                        onClick = remember(onPerkClick, perk) { { onPerkClick(perk) } },
                        modifier = Modifier.padding(
                            start = if (index == 0) 16.dp else 6.dp,
                            end = if (index == distinctPerks.lastIndex) 16.dp else 6.dp,
                        ),
                    )
                }
            }
        }
    }
}
