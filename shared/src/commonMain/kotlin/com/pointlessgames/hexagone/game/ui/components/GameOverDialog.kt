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
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.model.RankingInfo
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.game_over_title
import hexagone.shared.generated.resources.new_best_label
import hexagone.shared.generated.resources.stat_level
import hexagone.shared.generated.resources.stat_max_combo
import hexagone.shared.generated.resources.stat_merges
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GameOverDialog(
    modifier: Modifier = Modifier,
    score: Int,
    bestScore: Int,
    level: Int,
    maxCombo: Int,
    totalMerges: Int,
    highestValue: Int,
    rankingInfo: RankingInfo?,
    onViewBoard: () -> Unit,
) {
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(1500),
        label = "score_count_up",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "gameover_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow_alpha",
    )

    val isNewBest = score >= bestScore && score > 0
    val badgeTransition = rememberInfiniteTransition(label = "new_best_pulse")
    val badgeScale by badgeTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(animation = tween(800), repeatMode = RepeatMode.Reverse),
        label = "badge_scale",
    )
    val badgeRotation by badgeTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "badge_rotation",
    )

    val maxPieceTransition = rememberInfiniteTransition(label = "max_piece_shift")
    val maxPieceShift by maxPieceTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "max_piece_shift",
    )

    val spacing = MaterialTheme.spacing
    val cornerRadius = MaterialTheme.cornerRadius
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .drawBehind {
                val baseColor = primaryColor
                val cr = cornerRadius.extraLarge.toPx()
                val path = Path().apply {
                    addRoundRect(RoundRect(Rect(0f, 0f, size.width, size.height), cr, cr))
                }

                for (i in 1..3) {
                    drawPath(
                        path = path,
                        color = baseColor.copy(alpha = glowAlpha / (i * 2f)),
                        style = Stroke(width = (spacing.extraSmall * i).toPx()),
                    )
                }
            }
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                RoundedCornerShape(cornerRadius.extraLarge),
            )
            .border(
                spacing.extraTiny,
                Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(cornerRadius.extraLarge),
            )
            .padding(spacing.extraLarge),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(spacing.extraHuge)
                .clip(CircleShape)
                .clickable { onViewBoard() }
                .padding(spacing.small),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = spacing.extraTiny.toPx() * 1.5f
                val color = Color.White.copy(alpha = 0.4f)
                val path = Path().apply {
                    moveTo(0f, size.height / 2)
                    quadraticTo(size.width / 2, -size.height / 4, size.width, size.height / 2)
                    quadraticTo(size.width / 2, size.height * 1.25f, 0f, size.height / 2)
                }
                drawPath(path, color, style = Stroke(stroke))
                drawCircle(color, radius = size.width / 5, center = center, style = Stroke(stroke))
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(Res.string.game_over_title).uppercase(),
                color = Color.White.copy(alpha = 0.3f),
                fontWeight = FontWeight.Black,
                fontSize = 10.sp,
                letterSpacing = 4.sp,
            )

            Spacer(Modifier.height(spacing.small))

            AnimatedContent(rankingInfo) { rankingInfo ->
                rankingInfo?.let { rank ->
                    Box(
                        modifier = Modifier
                            .background(
                                Color.White.copy(alpha = 0.05f),
                                RoundedCornerShape(cornerRadius.full),
                            )
                            .border(
                                spacing.extraTiny,
                                Color.White.copy(alpha = 0.1f),
                                RoundedCornerShape(cornerRadius.full),
                            )
                            .padding(horizontal = spacing.medium, vertical = spacing.extraSmall),
                    ) {
                        Text(
                            text = if (rank.isRegional) "#${rank.rank} in your region" else "#${rank.rank} in the world",
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                        )
                    }
                    Spacer(Modifier.height(spacing.medium))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        Text(
                            text = animatedScore.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 72.sp,
                            textAlign = TextAlign.Center,
                            style = androidx.compose.ui.text.TextStyle(
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha * 2f),
                                    offset = Offset(0f, 0f),
                                    blurRadius = 30f,
                                ),
                            ),
                            modifier = Modifier.padding(horizontal = spacing.semiLarge),
                        )

                        if (isNewBest) {
                            Box(
                                modifier = Modifier
                                    .offset(x = spacing.semiMedium, y = -spacing.semiSmall)
                                    .graphicsLayer {
                                        scaleX = badgeScale
                                        scaleY = badgeScale
                                        rotationZ = badgeRotation
                                    }
                                    .background(
                                        MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(cornerRadius.full),
                                    )
                                    .padding(horizontal = spacing.small, vertical = spacing.tiny),
                            ) {
                                Text(
                                    text = stringResource(Res.string.new_best_label).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 8.sp,
                                )
                            }
                        }
                    }

                    Text(
                        text = "BEST: $bestScore",
                        color = Color.White.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                    )
                }

                Spacer(Modifier.width(spacing.extraLarge))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center) {
                        val colorScheme = MaterialTheme.colorScheme
                        val baseColor =
                            HexagonGridDefaults.getColorForValue(highestValue, colorScheme)
                        val shiftColor = Color(
                            (baseColor.red * 0.6f + 0.4f).coerceIn(0f, 1f),
                            (baseColor.green * 0.6f + 0.4f).coerceIn(0f, 1f),
                            (baseColor.blue * 0.6f + 0.4f).coerceIn(0f, 1f),
                            1f,
                        )

                        val c1 =
                            androidx.compose.ui.graphics.lerp(baseColor, shiftColor, maxPieceShift)
                        val c2 =
                            androidx.compose.ui.graphics.lerp(shiftColor, baseColor, maxPieceShift)

                        Hexagon(
                            value = highestValue.toString(),
                            backgroundColor = Color.Transparent,
                            modifier = Modifier
                                .size(spacing.giant)
                                .aspectRatio(1 / 0.866f)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(c1, c2, c1),
                                        start = Offset(0f, 0f),
                                        end = Offset.Infinite,
                                    ),
                                    shape = FlatTopHexagonShape(),
                                )
                                .border(
                                    spacing.extraTiny,
                                    Color.White.copy(alpha = 0.15f),
                                    FlatTopHexagonShape(),
                                ),
                        )
                    }
                    Spacer(Modifier.height(spacing.extraSmall))
                    Text(
                        text = "MAX",
                        color = Color.White.copy(alpha = 0.3f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp,
                    )
                }
            }

            Spacer(Modifier.height(spacing.extraHuge))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.small),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                OrganicStatItem(
                    icon = { LevelIcon(spacing) },
                    label = stringResource(Res.string.stat_level),
                    value = level.toString(),
                )
                OrganicStatItem(
                    icon = { ComboIcon(spacing) },
                    label = stringResource(Res.string.stat_max_combo),
                    value = "x$maxCombo",
                )
                OrganicStatItem(
                    icon = { MergeIcon(spacing) },
                    label = stringResource(Res.string.stat_merges),
                    value = totalMerges.toString(),
                )
            }
        }
    }
}
