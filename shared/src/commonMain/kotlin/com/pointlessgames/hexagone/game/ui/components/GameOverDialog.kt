package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.model.RankingInfo
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GameOverDialog(
    modifier: Modifier = Modifier,
    score: Int,
    bestScore: Int,
    level: Int,
    maxCombo: Int,
    @Suppress("UNUSED_PARAMETER") totalMerges: Int,
    highestValue: Int,
    rankingInfo: RankingInfo?,
    onViewBoard: () -> Unit,
) {
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(1500),
        label = "score_count_up",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "gameover_animations")
    
    val scoreGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "score_glow",
    )

    val maxPieceGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "max_piece_glow",
    )

    val isNewBest = score >= bestScore && score > 0
    
    val badgeRotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "badge_rotation",
    )

    val spacing = MaterialTheme.spacing

    Box(
        modifier = modifier
            .fillMaxWidth(0.92f)
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                RoundedCornerShape(48.dp),
            )
            .border(
                2.dp,
                Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(48.dp),
            )
            .padding(spacing.extraLarge),
    ) {
        HexagonIconButton(
            onClick = onViewBoard,
            icon = Res.drawable.ic_back,
            tooltip = Res.string.tooltip_view_board,
            size = 48.dp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .graphicsLayer { rotationZ = 180f }
                .padding(spacing.small),
            backgroundColor = Color.White.copy(alpha = 0.05f),
            borderColor = Color.White.copy(alpha = 0.1f)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(Res.string.game_over_title).uppercase(),
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                letterSpacing = 6.sp,
            )

            Spacer(Modifier.height(spacing.medium))

            AnimatedContent(rankingInfo) { rankingInfo ->
                rankingInfo?.let { rank ->
                    Box(
                        modifier = Modifier
                            .background(
                                Color.White.copy(alpha = 0.08f),
                                RoundedCornerShape(MaterialTheme.cornerRadius.full),
                            )
                            .padding(horizontal = spacing.large, vertical = spacing.extraSmall),
                    ) {
                        Text(
                            text = if (rank.isRegional) "#${rank.rank} in your region" else "#${rank.rank} in the world",
                            color = Color(0xFFFFD54F), // Yellow from screenshot
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                        )
                    }
                    Spacer(Modifier.height(spacing.medium))
                }
            }

            Spacer(Modifier.height(spacing.small))

            Box(contentAlignment = Alignment.TopEnd) {
                Text(
                    text = animatedScore.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 84.sp,
                    textAlign = TextAlign.Center,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color(0xFFFFD54F).copy(alpha = scoreGlowAlpha),
                            offset = Offset(0f, 0f),
                            blurRadius = 40f,
                        ),
                    ),
                    modifier = Modifier.padding(horizontal = spacing.extraLarge),
                )

                if (isNewBest) {
                    Box(
                        modifier = Modifier
                            .offset(x = 16.dp, y = -8.dp)
                            .graphicsLayer {
                                rotationZ = badgeRotation
                            }
                            .background(
                                Color(0xFFF06292), // Pink from screenshot
                                RoundedCornerShape(MaterialTheme.cornerRadius.full),
                            )
                            .padding(horizontal = spacing.medium, vertical = spacing.extraSmall),
                    ) {
                        Text(
                            text = stringResource(Res.string.new_best_label).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            val bestScoreLabel = if (score > bestScore && score > 0) {
                stringResource(Res.string.previous_best_label, bestScore)
            } else {
                stringResource(Res.string.best_score_formatted, bestScore)
            }

            Text(
                text = bestScoreLabel,
                color = Color.White.copy(alpha = 0.4f),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                letterSpacing = 1.sp,
            )

            Spacer(Modifier.height(spacing.extraHuge))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Level Stat
                GameOverStatHexagon(
                    label = stringResource(Res.string.label_level),
                    value = level.toString(),
                    backgroundColor = Color(0xFF37474F), // Dark teal/grey
                    size = 72.dp
                )

                // Max Piece Stat (Larger and glowing)
                GameOverStatHexagon(
                    label = stringResource(Res.string.label_max_piece),
                    value = highestValue.toString(),
                    backgroundColor = Color(0xFF9345C4), // Purple
                    size = 92.dp,
                    labelColor = Color.White,
                    glowAlpha = maxPieceGlowAlpha,
                    glowColor = Color(0xFFBB86FC)
                )

                // Max Combo Stat
                GameOverStatHexagon(
                    label = stringResource(Res.string.label_max_combo),
                    value = maxCombo.toString(),
                    backgroundColor = Color(0xFF5D4037), // Dark brown
                    size = 72.dp
                )
            }
        }
    }
}

@Composable
private fun GameOverStatHexagon(
    label: String,
    value: String,
    backgroundColor: Color,
    size: Dp,
    labelColor: Color = Color.White.copy(alpha = 0.6f),
    glowAlpha: Float = 0f,
    glowColor: Color = Color.Transparent
) {
    val spacing = MaterialTheme.spacing
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.medium)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (glowAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .size(width = size + 16.dp, height = (size + 16.dp) * 0.866f)
                        .graphicsLayer { alpha = glowAlpha }
                        .background(glowColor.copy(alpha = 0.4f), FlatTopHexagonShape())
                )
            }
            Box(
                modifier = Modifier
                    .size(width = size, height = size * 0.866f)
                    .background(backgroundColor, FlatTopHexagonShape())
                    .border(2.dp, Color.White.copy(alpha = 0.1f), FlatTopHexagonShape()),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = value,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = (size.value * 0.35f).sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        Text(
            text = label,
            color = labelColor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 0.5.sp
        )
    }
}
